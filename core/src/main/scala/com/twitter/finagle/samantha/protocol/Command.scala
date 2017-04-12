package com.twitter.finagle.samantha.protocol

import com.twitter.io.Buf

abstract class Command {
  def name: Buf
  def body: Seq[Buf] = Seq.empty
  def prefix: Option[String] = None
  def suffix: Option[String] = None
}

object Command {
  
  val EOL = Buf.Utf8("\r\n")
  val CR  = Buf.Utf8("\r")
  
  private[samantha] def encode(c: Command): Buf = {
    val command = c.name +: c.body
    val prefix = Buf.Utf8(c.prefix.getOrElse(""))
    val suffix = Buf.Utf8(c.suffix.getOrElse(""))
    
    val bufs = command flatMap { cmd =>
      Vector(prefix, cmd, suffix)
    }
    Buf(bufs)
  }
  
}

case class GlobalCacheCommand(data: String) extends Command {
  def name: Buf = Buf.Utf8(data)
}

case class HexCommand(data: String) extends Command {
  def name: Buf = Buf.Utf8(data)
}

case class NetworkCommand(data: String) extends Command {
  def name: Buf = Buf.Utf8(data)
}

case class RawCommand(data: String) extends Command {
  def name: Buf = Buf.Utf8(data)
}

