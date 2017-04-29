package com.twitter.finagle
package samantha
package network

import java.util.concurrent.atomic.AtomicReference

import com.twitter.io.Buf
import com.twitter.finagle.dispatch.GenSerialClientDispatcher
import com.twitter.finagle.samantha.protocol._
import com.twitter.finagle.transport.Transport
import com.twitter.util.{Future, Promise}

import scala.util.control.NonFatal

sealed trait NetworkHandler {
  def onSuccess(node: Service[Buf, Buf]): Unit
  def onException(node: Service[Buf, Buf], ex: Throwable): Unit
  def onFeedback(fb: Feedback): Unit
}

class NetworkDispatcher(trans: Transport[Buf, Buf], config: Config)
  extends GenSerialClientDispatcher[Buf, Buf, Buf, Buf](trans) {
  
  private val handler = new AtomicReference[NetworkHandler]
  
  //private[this] def processAndRead: Buf => Future[Unit] = buf => { loop() }
  
  private[this] def loop(): Unit = {
    trans.read().onSuccess { rep =>
      if (rep.length > 0) println(Buf.Utf8.unapply(rep).get)
      loop()
    }.onFailure {
      case NonFatal(ex) => println(ex.getMessage)
    }
    //trans.read().flatMap(processAndRead)
  }
  
  //loop().onFailure { _ => trans.close() }
  loop()
  
  protected def dispatch(req: Buf, p: Promise[Buf]): Future[Unit] = {
    trans.write(req)
      .onSuccess { _ => p.setValue(Buf.Empty) }
      .onFailure { case NonFatal(ex) => p.setException(ex) }
  }
  
  override def apply(buf: Buf): Future[Buf] = {
    handler.compareAndSet(null, handler.get())
    super.apply(buf)
  }
  
  override def close(deadline: com.twitter.util.Time) = {
    super.close(deadline)
  }
  
}
