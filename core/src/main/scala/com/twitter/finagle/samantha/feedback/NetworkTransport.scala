package com.twitter.finagle
package samantha.feedback

import java.nio.charset.Charset
import java.security.cert.Certificate

import com.twitter.finagle.samantha.protocol._
import com.twitter.finagle.transport.Transport
import com.twitter.io.Buf
import com.twitter.util.{Future, Time}

case class NetworkTransport(
    underlying: Transport[Buf, Buf])
  extends Transport[Command, Feedback] {
  
  private[this] val Utf8 = Charset.forName("UTF-8")
  
  private[this] val Delimiter = "\r\n"
  
  @volatile private[this] var buf: Buf = Buf.Empty
  
  def write(c: Command): Future[Unit] =
    underlying.write(Command.encode(c))
  
  def read(): Future[Feedback] = buf match {
    case Buf.Utf8(str) if str.contains(Delimiter) =>
      val feedback = str.split(Delimiter)
      buf = feedback.mkString(Delimiter) match {
        case "" =>
          Buf.Empty
        case newStr =>
          val post = if (str.endsWith(Delimiter)) Delimiter else ""
          Buf.Utf8(newStr + post)
      }
      Future(decode(feedback(0)))
    case _ =>
      underlying.read() flatMap { b =>
        buf = buf.concat(b)
        read()
      }
  }
  
  def status = underlying.status
  
  val onClose = underlying.onClose
  
  def localAddress = underlying.localAddress
  
  def remoteAddress = underlying.remoteAddress
  
  def close(deadline: Time) = underlying.close(deadline)
  
  override def peerCertificate: Option[Certificate] = underlying.peerCertificate
  
  private[this] def decode(cmdStr: String): Feedback = {
    TextualFeedback(cmdStr)
  }
}