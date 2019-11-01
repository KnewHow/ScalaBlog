package scala.blog.safeMap

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
