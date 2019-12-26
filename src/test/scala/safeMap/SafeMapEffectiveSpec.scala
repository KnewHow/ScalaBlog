package scala.blog.safeMap

import cats._
import cats.data._
import cats.effect._
import cats.instances.vector._
import cats.syntax.all._
import java.util.concurrent._
import scala.concurrent.ExecutionContext
import org.scalatest.FlatSpec
import cats.effect.concurrent.Ref

class SafeMapEffectiveSpec extends FlatSpec {
  "test safe map put and get" should "success" in {
    implicit val sio = Sync[IO].delay(println("Hello world!"))
    val safeMap:IO[SafeMap[IO,String,String]] = SafeMap.empty[IO,String,String]
    val ex = Executors.newFixedThreadPool(10);
    implicit val ctx = IO.contextShift(ExecutionContext.fromExecutor(ex))
    val seq = Seq.iterate(0,1000000)(_ + 1).map(_.toString)
    val currentMap = new ConcurrentHashMap[String,String]()
    val r1 = seq.map{r =>
      IO.delay(currentMap.putIfAbsent("how",r))
    }.toVector.parSequence
    val r2 = safeMap.flatMap{map =>
      seq.map{r =>
        map.putIfAbsent("how", r)
      }.toVector.parSequence
    }
    val b1 = System.currentTimeMillis
    r1.unsafeRunSync
    val b2 = System.currentTimeMillis
    r2.unsafeRunSync
    val e = System.currentTimeMillis
    println(s"currentMap took->${b2-b1}")
    println(s"safeMap took->${e-b2}")
    succeed
  }
}
