package scala.blog.safeMap

import cats._
import cats.effect._
import cats.effect.concurrent._
import cats.instances.vector._
import cats.syntax.all._
import org.scalatest.FlatSpec

class SafeMapSpec extends FlatSpec {
  "test safe map put and get" should "success" in {
    implicit val sio = Sync[IO].delay(println("Hello world!"))
    val mapF = SafeMap.empty[IO,String,Int]
    val key = "how"
    val value = 23
    val fut = for {
      map <- mapF
      _ <- map.putIfAbsent(key, value)
      r <- map.get(key)
    } yield r.exists(_  == value)

    assert(fut.unsafeRunSync)
  }
}
