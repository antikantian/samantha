package io.samantha

import java.nio.charset.Charset

import cats.Show
import com.twitter.io.Buf

trait Encode[A] {
  type ContentType <: String
  
  def apply(a: A, cs: Charset): Buf
}

trait LowPriorityEncodeInstances {
  type Aux[A, CT <: String] = Encode[A] { type ContentType = CT }
  
  final def instance[A, CT <: String](f: (A, Charset) => Buf): Aux[A, CT] =
    new Encode[A] {
      type ContentType = CT
      def apply(a: A, cs: Charset): Buf = f(a, cs)
    }
}

trait HighPriorityEncodeInstances extends LowPriorityEncodeInstances {
  private[this] final val anyToEmptyBuf: Aux[Any, Nothing] =
    instance[Any, Nothing]((_, _) => Buf.Empty)
  
  private[this] final val bufToBuf: Aux[Buf, Nothing] =
    instance[Buf, Nothing]((buf, _) => buf)
  
  implicit def encodeUnit[CT <: String]: Aux[Unit, CT] =
    anyToEmptyBuf.asInstanceOf[Aux[Unit, CT]]
  
  implicit def encodeException[CT <: String]: Aux[Exception, CT] =
    anyToEmptyBuf.asInstanceOf[Aux[Exception, CT]]
  
  implicit def encodeBuf[CT <: String]: Aux[Buf, CT] =
    bufToBuf.asInstanceOf[Aux[Buf, CT]]
}

object Encode extends HighPriorityEncodeInstances {
  @inline final def apply[A, CT <: String](implicit e: Aux[A, CT]): Aux[A, CT] = e
  

}