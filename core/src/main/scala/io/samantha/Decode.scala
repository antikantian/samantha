package io.samantha

import java.nio.charset.Charset

import com.twitter.io.Buf
import com.twitter.util.Try

trait Decode[A] {
  type ContentType <: String
  
  def apply(b: Buf, cs: Charset): Try[A]
}

object Decode {
  
  type Aux[A, CT <: String] = Decode[A] { type ContentType = CT }
  
  def instance[A, CT <: String](f: (Buf, Charset) => Try[A]): Aux[A, CT] =
    new Decode[A] {
      type ContentType = CT
      def apply(b: Buf, cs: Charset): Try[A] = f(b, cs)
    }
  
  @inline def apply[A, CT <: String](implicit d: Aux[A, CT]): Aux[A, CT] = d
  
}