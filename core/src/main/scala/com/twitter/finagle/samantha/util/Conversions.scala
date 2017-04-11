package com.twitter.finagle
package samantha.util

import com.twitter.finagle.samantha.protocol._
import com.twitter.io.Buf
import java.nio.charset.{ Charset, StandardCharsets }
import org.jboss.netty.buffer.{ ChannelBuffer, ChannelBuffers }

trait ErrorConversion {
  def getException(msg: String): Throwable
  
  def apply(requirement: Boolean, message: String = "Prerequisite failed") {
    if (!requirement) {
      throw getException(message)
    }
  }
  def safe[T](fn: => T): T = {
    try {
      fn
    } catch {
      case e: Throwable => throw getException(e.getMessage)
    }
  }
}

object BytesToString {
  def apply(arg: Array[Byte], charset: Charset = StandardCharsets.UTF_8) = new String(arg, charset)
  
  def fromList(args: Seq[Array[Byte]], charset: Charset = StandardCharsets.UTF_8) =
    args.map { arg => BytesToString(arg, charset) }
  
  def fromTuples(args: Seq[(Array[Byte], Array[Byte])], charset: Charset = StandardCharsets.UTF_8) =
    args map { arg => (BytesToString(arg._1), BytesToString(arg._2)) }
  
  def fromTuplesWithDoubles(args: Seq[(Array[Byte], Double)],
    charset: Charset = StandardCharsets.UTF_8) =
    args map { arg => (BytesToString(arg._1, charset), arg._2) }
}

object StringToBytes {
  def apply(arg: String, charset: Charset = StandardCharsets.UTF_8) = arg.getBytes(charset)
  def fromList(args: List[String], charset: Charset = StandardCharsets.UTF_8) =
    args.map { arg =>
      arg.getBytes(charset)
    }
}

object StringToChannelBuffer {
  def apply(string: String, charset: Charset = StandardCharsets.UTF_8) = {
    ChannelBuffers.wrappedBuffer(string.getBytes(charset))
  }
}

object StringToBuf {
  def apply(string: String): Buf = Buf.Utf8(string)
}

object BufToString {
  def apply(buf: Buf): String = Buf.Utf8.unapply(buf).get
}

object CBToString {
  def apply(arg: ChannelBuffer, charset: Charset = StandardCharsets.UTF_8) = {
    arg.toString(charset)
  }
  def fromList(args: Seq[ChannelBuffer], charset: Charset = StandardCharsets.UTF_8) =
    args.map { arg => CBToString(arg, charset) }
  
  def fromTuples(
    args: Seq[(ChannelBuffer, ChannelBuffer)], charset: Charset = StandardCharsets.UTF_8
  ) = args map { arg => (CBToString(arg._1), CBToString(arg._2)) }
  
  def fromTuplesWithDoubles(
    args: Seq[(ChannelBuffer, Double)],
    charset: Charset = StandardCharsets.UTF_8
  ) = args map { arg => (CBToString(arg._1, charset), arg._2) }
}

object FeedbackFormat {
  def toString(items: List[Feedback]): List[String] = {
    items flatMap {
      case BulkFeedback(message)    => List(BufToString(message))
      case EmptyBulkFeedback        => EmptyBulkFeedbackString
      case IntegerFeedback(id)      => List(id.toString)
      case TextualFeedback(message) => List(message)
      case ErrorFeedback(message)   => List(message)
      case _                        => Nil
    }
  }
  
  def toBuf(items: List[Feedback]): List[Buf] = {
    items flatMap {
      case BulkFeedback(message)   => List(message)
      case EmptyBulkFeedback       => EmptyBulkFeedbackChannelBuffer
      case IntegerFeedback(id)     => List(Buf.ByteArray.Owned(Array(id.toByte)))
      case TextualFeedback(message) => List(Buf.Utf8(message))
      case ErrorFeedback(message)  => List(Buf.Utf8(message))
      case _                    => Nil
    }
  }
  
  private val EmptyBulkFeedbackString: List[String] = List("nil")
  private val EmptyBulkFeedbackChannelBuffer: List[Buf] = List(Buf.Empty)
}
