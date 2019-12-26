# SafeMap —— 媲美 ConcurrentHashMap 的存在

如果你学Java有较长的时间，肯定知道Java中有一个线程安全的Map：`ConcurrentHashMap`，说起 `ConcurrentHashMap`，那可不得了，线程安全、性能高、还有面试经常被问。网上有很多关于`ConcurrentHashMap`文章，如果有不懂的小伙伴可以参阅：[Map 综述（三）：彻头彻尾理解 ConcurrentHashMap](https://blog.csdn.net/justloveyou_/article/details/72783008)。

看了文章后，你应该知道，`ConcurrentHashMap`解决线程安全问题的方式还是使用**锁**机制，只不过它的锁粒度更小而已，在高并发的情况下，还是会存在线程被阻塞的情况。今天我们就来介绍一种不阻塞的`SafeMap`，它的底层实现方式采用**CAS**机制。

那么什么是 CAS 呢？CAS全称为 Compare And Swap，翻译过来就是比较并替换。他使用乐观的锁的机制来保证线程的安全，例如有两个线程同时修改一个变量`A`的值，那么其中一个线程会成功，另一个线程会失败，最重要的是失败的那个线程不会被**阻塞**，它可以再次进行尝试。想了解更多有关 CAS 可以参考：[漫画：什么是 CAS 机制？](https://blog.csdn.net/bjweimengshu/article/details/78949435) 和 [漫画：什么是CAS机制？（进阶篇）](https://blog.csdn.net/bjweimengshu/article/details/79000506)。

在正式的给出代码前，我们先来了解 Scala cats 中使用 CAS 机制实现的并发原语[Ref](https://typelevel.org/cats-effect/concurrency/ref.html)：

他是一个纯原子类型的引用，在通讯渠道上又是相互排斥的，它的`modify`方法是原子性的并且允许并发更新，可以用作并发计数器和缓存，而且可以用来构建更复杂的并发结构。

下面让我们来看一下使用`Ref`来实现的`SafeMap`:
```Scala
import cats._
import cats.effect._
import cats.effect.concurrent._
import cats.syntax.all._

class SafeMap[F[_], K, V](ref: Ref[F, Map[K, V]])(implicit F: Functor[F]) {
  def putIfAbsent(k: K, v: V): F[Option[V]] = ref.modify { m =>
    m.get(k) match {
      case None =>
        (m + (k -> v), None)
      case Some(v) =>
        (m, Some(v))
    }
  }

  def remove(k: K, old: V): F[Boolean] = ref.modify { m =>
    m.get(k) match {
      case Some(v) if v == old =>
        (m - k, true)
      case _ => (m, false)
    }
  }

  def get(k: K): F[Option[V]] = ref.get.map(_.get(k))
}

object SafeMap {
  def empty[F[_]: Sync, K, V] =
    Ref.of[F, Map[K, V]](Map.empty[K, V]).map(new SafeMap(_))
}
```

30多行的代码就解决了线程安全、高性能等诸多问题，是不是不敢相信？

到这里，或许还有小伙伴会质疑`SafeMap`的性能，我们使用下面的测试用例来比较`ConcurrentHashMap` 和 `SafeMap` 的性能：

```Scala
import cats._
import cats.data._
import cats.effect._
import cats.instances.vector._
import cats.syntax.all._
import java.util.concurrent._
import scala.concurrent.ExecutionContext
import org.scalatest.FlatSpec
import cats.effect.concurrent.Ref
import scala.concurrent.ExecutionContext.global

class SafeMapEffectiveSpec extends FlatSpec {
  "test safe map put and get" should "success" in {
    implicit val sio = Sync[IO].delay(println("Hello world!"))
    val safeMap:IO[SafeMap[IO,String,String]] = SafeMap.empty[IO,String,String]
    implicit val ctx = IO.contextShift(global)
    val seq = Seq.iterate(0,1000000)(_ + 1).map(_.toString)
    val currentMap = new ConcurrentHashMap[String,String]()
    // 使用多个线程对两个 map 分别进行 putIfAbsent 操作
    val r1 = seq.map{r =>
      IO.delay(currentMap.putIfAbsent("how",r))
    }.toVector.parSequence // 并行执行
    val r2 = safeMap.flatMap{map =>
      seq.map{r =>
        map.putIfAbsent("how", r)
      }.toVector.parSequence  // 并行执行
    }
    val b1 = System.currentTimeMillis
    r1.unsafeRunSync
    val b2 = System.currentTimeMillis
    r2.unsafeRunSync
    val e = System.currentTimeMillis
    println(s"concurrentHashMap took->${b2-b1}")
    println(s"safeMap took->${e-b2}")
    succeed
  }
}
```

运行结果：
```
concurrentHashMap took->1790
safeMap took->2046
```

虽然在性能上`SafeMap`稍逊`ConcurrentHashMap`一点点，但是实现更轻量，而且不会阻塞的线程的`SafeMap`是全异步架构的首选！