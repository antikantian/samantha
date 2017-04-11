//package com.twitter.finagle.samantha.feedback
//
//import java.util.concurrent.atomic.AtomicReference
//
//import com.twitter.finagle.dispatch.GenSerialClientDispatcher
//import com.twitter.finagle.samantha.protocol._
//import com.twitter.finagle.samantha.transport._
//import com.twitter.finagle.transport.Transport
//import com.twitter.util.{Future, Promise}
//
//import scala.util.control.NonFatal
//
//class FeedbackClientDispatcher(trans: Transport[Command, Feedback])
//  extends GenSerialClientDispatcher[Command, Feedback, Command, Feedback](trans) {
//
//  private val handler = new AtomicReference[FeedbackClientDispatcher]
//
//  loop()
//
//  private[this] def loop(): Unit =
//    trans.read().onSuccess { reply =>
//      handler.get()
//      loop()
//    }.onFailure {
//      case NonFatal(ex) =>
//        Option(handler.get()).foreach(_.onException(this, ex))
//    }
//
//  protected def dispatch(req: Message, p: Promise[Message]): Future[Unit] = {
//    trans.write(req)
//      .onSuccess { _ => p.setValue(Message.EmptyMessage) }
//      .onFailure { case NonFatal(ex) => p.setException(ex) }
//  }
//
//  override def apply(req: Command): Future[Command] = {
//    req match {
//      case msg: Message =>
//        handler.compareAndSet(null, handler.get())
//      case _ =>
//        throw new IllegalArgumentException("Not a subscribe/unsubscribe command")
//    }
//  }
//
//  override def close(deadline: com.twitter.util.Time) = {
//    super.close(deadline)
//  }
//
//
//}
