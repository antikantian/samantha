package com.twitter.finagle.samantha.protocol

import com.twitter.io.Buf

trait Command {
  def name: Buf
  def body: Seq[Buf]
  def prefix: Option[String]
  def suffix: Option[String]
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
  val name: Buf = Buf.Utf8(data)
  val body: Seq[Buf] = Seq.empty[Buf]
  val prefix: Option[String] = None
  val suffix: Option[String] = None
  
}

case class HexCommand(data: String) extends Command {
  def name: Buf = Buf.Utf8(data)
  val body: Seq[Buf] = Seq.empty[Buf]
  val prefix: Option[String] = None
  val suffix: Option[String] = None
}

case class NetworkCommand(data: String) extends Command {
  def name: Buf = Buf.Utf8(data)
  val body: Seq[Buf] = Seq.empty[Buf]
  val prefix: Option[String] = None
  val suffix: Option[String] = None
}

case class RawCommand(data: String, sendBefore: Option[String], sendAfter: Option[String]) extends Command {
  val name: Buf = Buf.Utf8(data)
  val body: Seq[Buf] = Seq.empty[Buf]
  val prefix: Option[String] = sendBefore
  val suffix: Option[String] = sendAfter
}

