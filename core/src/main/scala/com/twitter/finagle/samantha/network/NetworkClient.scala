package com.twitter.finagle
package samantha.network

import com.twitter.io.Buf
import com.twitter.finagle.samantha.protocol._
import com.twitter.finagle.util.DefaultTimer
import com.twitter.util.{Closable, Future, Time, Timer}

object NetworkClient {
  
  def apply(host: String): NetworkClient =
    new NetworkClient(com.twitter.finagle.Samantha.client.newClient(host))
  
  def apply(raw: ServiceFactory[Buf, Buf]): NetworkClient =
    new NetworkClient(raw)
}

class NetworkClient(
  override val factory: ServiceFactory[Buf, Buf],
  private[samantha] val timer: Timer = DefaultTimer.twitter)
  extends BaseNetworkClient(factory)

abstract class BaseNetworkClient(
  protected val factory: ServiceFactory[Buf, Buf])
  extends Closable {
  
  def close(deadline: Time): Future[Unit] = factory.close(deadline)
  
//  private[samantha] def doRequest[T](cmd: Command)(handler: PartialFunction[Feedback, Future[T]]): Future[T] = {
//    factory.toService.apply(cmd).flatMap (handler orElse {
//      case ErrorFeedback(message)     => Future.exception(new Exception(message))
//      case TextualFeedback("QUEUED")  => Future.Done.asInstanceOf[Future[Nothing]]
//      case _                          => Future.exception(new IllegalStateException)
//    })
//  }
  
}
