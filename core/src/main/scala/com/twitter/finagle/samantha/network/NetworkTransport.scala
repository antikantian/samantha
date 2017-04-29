package com.twitter.finagle
package samantha.network

import java.net.SocketAddress
import java.security.cert.Certificate

import com.twitter.finagle.transport.Transport
import com.twitter.io.Buf
import com.twitter.finagle.samantha.protocol._
import com.twitter.util.{Future, Time}

private[finagle] final class NetworkTransport(
    underlying: Transport[Buf, Buf]
) extends Transport[Buf, Buf] {
  
  private[this] def readLoop(buf: Buf): Future[Buf] = Future.value(buf)
  
  def sendQuery(buf: Buf): Future[Buf] =
    for {
      _ <- write(buf)
      r <- read()
    } yield r
  
  def write(buf: Buf): Future[Unit] = underlying.write(buf)
  
  def read(): Future[Buf] = readLoop(Buf.Empty)
  
  def close(deadline: Time): Future[Unit] = underlying.close(deadline)
  
  override def status: Status = underlying.status
  
  override def onClose: Future[Throwable] = underlying.onClose
  
  override def localAddress: SocketAddress = underlying.localAddress
  
  override def remoteAddress: SocketAddress = underlying.remoteAddress
  
  override def peerCertificate: Option[Certificate] = underlying.peerCertificate
  
}