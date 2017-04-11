package com.twitter.finagle
package samantha.protocol

import java.net.SocketAddress
import java.security.cert.Certificate

import com.twitter.finagle.transport.Transport
import com.twitter.io.Buf
import com.twitter.util.{Future, Time}

private[finagle] final class StageTransport(underlying: Transport[Buf, Buf])
  extends Transport[Command, Feedback] {
  
  private[this] val decoder = new StageDecoder(Feedback.decode)
  
  private[this] def readLoop(buf: Buf): Future[Feedback] = decoder.absorb(buf) match {
    case null => underlying.read().flatMap(readLoop)
    case reply =>
      println(s"[Transport]Inbound: $reply")
      Future.value(reply)
  }
  
  def write(c: Command): Future[Unit] = underlying.write(Command.encode(c))
  
  def read(): Future[Feedback] = readLoop(Buf.Empty)
  
  def close(deadline: Time): Future[Unit] = underlying.close(deadline)
  
  override def status: Status = underlying.status
  
  override def onClose: Future[Throwable] = underlying.onClose
  
  override def localAddress: SocketAddress = underlying.localAddress
  
  override def remoteAddress: SocketAddress = underlying.remoteAddress
  
  override def peerCertificate: Option[Certificate] = underlying.peerCertificate
  
}