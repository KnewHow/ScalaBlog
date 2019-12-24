package scala.blog.safeMap

import cats._
import cats.effect._
import cats.instances.vector._
import cats.effect.concurrent._
import cats.syntax.all._
import java.util.concurrent._
import scala.concurrent.ExecutionContext
import cats.implicits._
import org.scalatest.FlatSpec
import scala.concurrent.ExecutionContext.Implicits.global

class SafeMapEffectiveSpec extends FlatSpec {
  "test safe map put and get" should "success" in {
    implicit val sio = Sync[IO].delay(println("Hello world!"))
    val safeMap = SafeMap.empty[IO,Int,Int]
    implicit val ctx = IO.contextShift(global)
    val seq = Seq.fill(1000)("how")
    val currentMap = new  ConcurrentHashMap[String,String]()
    // def testCurrentMap[F[_]]()(implicit F: ConcurrentEffect[F],P: Parallel[F]) {
    // 
    //   val a = Seq.iterate(0,1000)(_ + 1).map(_.toString).map(
    //      r => IO.pure(currentMap.putIfAbsent(r,r))
    //   ).toVector.parSequence
    // }
    // val r1 = seq.map{
    //   r => IO.pure(currentMap.putIfAbsent(r,r))
    // }.toVector.parSequence
    // IO.pure(1).map(_.toString)
    Seq.iterate(0,1000)(_ + 1).map(_.toString).map
    // val r2 = vector.map{r =>
    //   map.putIfAbsent(key, value)
    // }.toVector.parSequence
    // r1.unsafeRunSync
    succeed
  }
}
