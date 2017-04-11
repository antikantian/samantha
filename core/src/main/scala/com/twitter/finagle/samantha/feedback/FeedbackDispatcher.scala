package com.twitter.finagle.samantha.feedback

import java.util.concurrent.atomic.AtomicReference

import com.twitter.finagle.dispatch.GenSerialClientDispatcher
import com.twitter.finagle.samantha.protocol._
import com.twitter.finagle.transport.Transport
import com.twitter.util.{Future, Promise}

import scala.util.control.NonFatal

class FeedbackDispatcher(trans: Transport[Command, Feedback])
  extends GenSerialClientDispatcher[Command, Feedback, Command, Feedback](trans) {
  
  private val handler = new AtomicReference[FeedbackHandler]
  
  loop()
  
  private[this] def loop(): Unit =
    trans.read().onSuccess { fb =>
      println(fb)
      //handler.get().onFeedback(fb)
      loop()
    }.onFailure {
      case NonFatal(ex) =>
        Option(handler.get()).foreach(_.onException(this, ex))
    }
  
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
