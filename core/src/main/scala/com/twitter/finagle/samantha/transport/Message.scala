package com.twitter.finagle.samantha.transport

import com.twitter.finagle.netty4.Bufs
import com.twitter.finagle.tracing.{Flags, SpanId, TraceId}
import com.twitter.finagle.{Dentry, Dtab, Failure, NameTree, Path}
import com.twitter.io.Buf
import com.twitter.util.{Duration, Time}
import java.nio.charset.{StandardCharsets => Charsets}
import scala.collection.mutable.ArrayBuffer

private[samantha] sealed trait Message {
  def data: Buf
}

object Message {
  
  case class Command(data: Buf) extends Message
  
  case class Response(data: Buf) extends Message
  
  case class Feedback(data: Buf) extends Message
  
  case class NetworkCommand(name: String, data: Buf) extends Message
  
  case class IRCommand(name: String, data: Buf, repeat: Int) extends Message
  
  case class AutoFeedback(
      name: String,
      device: String,
      prefix: Buf,
      suffix: Buf,
      data: Buf
  ) extends Message
  
  case class Error(data: Buf) extends Message
  
  case object EmptyMessage extends Message {
    val data: Buf = Buf.Empty
  }
  
}