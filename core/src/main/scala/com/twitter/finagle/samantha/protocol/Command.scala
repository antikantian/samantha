package com.twitter.finagle.samantha.protocol

import com.twitter.io.Buf

abstract class Command {
  def name: Buf
  def body: Seq[Buf] = Seq.empty
}

object Command {
  
  val EOL = Buf.Utf8("\r\n")
  val CR  = Buf.Utf8("\r")
  val ARG_COUNT         = Buf.Utf8("*")
  val ARG_SIZE          = Buf.Utf8("$")
  
  private[samantha] def encode(c: Command): Buf = {
    val args = c.name +: c.body
    
    val header: Vector[Buf] = Vector(
      ARG_COUNT, Buf.Utf8(args.length.toString), EOL)
    
    val bufs = args.flatMap { arg =>
      Vector(arg, CR)
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

