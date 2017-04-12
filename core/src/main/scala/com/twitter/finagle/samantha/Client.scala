package com.twitter.finagle
package samantha

import com.twitter.finagle.samantha.protocol._
import com.twitter.finagle.util.DefaultTimer
import com.twitter.util.{Closable, Future, Time, Timer}

object Client {
  
  def apply(host: String): Client =
    new Client(com.twitter.finagle.Samantha.client.newClient(host))
  
  def apply(raw: ServiceFactory[Command, Feedback]): Client =
    new Client(raw)
}

class Client(
  override val factory: ServiceFactory[Command, Feedback],
  private[samantha] val timer: Timer = DefaultTimer.twitter)
  extends BaseClient(factory)

abstract class BaseClient(
  protected val factory: ServiceFactory[Command, Feedback])
  extends Closable {
  
  def close(deadline: Time): Future[Unit] = factory.close(deadline)
  
  private[samantha] def doRequest[T](cmd: Command)(handler: PartialFunction[Feedback, Future[T]]): Future[T] = {
    factory.toService.apply(cmd).flatMap (handler orElse {
      case ErrorFeedback(message)     => Future.exception(ServerError(message))
      case TextualFeedback("QUEUED")  => Future.Done.asInstanceOf[Future[Nothing]]
      case _                          => Future.exception(new IllegalStateException)
    })
  }
}
