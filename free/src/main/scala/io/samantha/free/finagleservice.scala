package io.samantha
package free

import cats.Monad
import cats.data.Kleisli
import com.twitter.finagle.Service

/**
  * Algebra and free monad for primitive operations over a `com.twitter.finagle.Service[Req, Rep]`.
  */
object finagleservice {
  
  sealed trait FinagleServiceOp[A] {
    protected def primitive[M[_]: Monad, I, O](f: Service[I, O] => A): Kleisli[M, Service[I, O], A] =
      Kleisli((s: Service[I, O]) => )
  }
  
}