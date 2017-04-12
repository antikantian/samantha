package com.twitter.finagle
package samantha.network

import java.util.concurrent.atomic.AtomicReference

import com.twitter.concurrent.Broker
import com.twitter.finagle.dispatch.GenSerialClientDispatcher
import com.twitter.finagle.samantha.protocol._
import com.twitter.finagle.transport.Transport
import com.twitter.util.{Future, Promise}

import scala.util.control.NonFatal

sealed trait NetworkHandler {
  def onSuccess(node: Service[Command, Feedback]): Unit
  def onException(node: Service[Command, Feedback], ex: Throwable): Unit
  def onFeedback(fb: Feedback): Unit
}

class NetworkDispatcher(trans: Transport[Command, Feedback], config: NetworkConfig)
  extends GenSerialClientDispatcher[Command, Feedback, Command, Feedback](trans) {
  
  private val handler = new AtomicReference[NetworkHandler]
  
  private[this] def processAndRead: Feedback => Future[Unit] =
    fb => { loop() }
  
  private[this] def loop(): Future[Unit] = trans.read().flatMap(processAndRead)
  
  loop().onFailure { _ => trans.close() }
  
  //  private[this] def loop(): Unit =
  //    trans.read().onSuccess { fb =>
  //      println(fb)
  //      //handler.get().onFeedback(fb)
  //      loop()
  //    }.onFailure {
  //      case NonFatal(ex) =>
  //        Option(handler.get()).foreach(_.onException(this, ex))
  //    }
  
  protected def dispatch(req: Command, p: Promise[Feedback]): Future[Unit] = {
    trans.write(req)
      .onSuccess { _ => p.setValue(NoFeedback) }
      .onFailure { case NonFatal(ex) => p.setException(ex) }
  }
  
  override def apply(req: Command): Future[Feedback] = {
    handler.compareAndSet(null, handler.get())
    super.apply(req)
  }
  
  override def close(deadline: com.twitter.util.Time) = {
    super.close(deadline)
  }
}