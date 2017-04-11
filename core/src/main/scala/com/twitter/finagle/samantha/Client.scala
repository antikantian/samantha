package com.twitter.finagle
package samantha

import com.twitter.finagle.netty3.ChannelBufferBuf
import com.twitter.finagle.{Service, ClientConnection, ServiceFactory, ServiceProxy}
import com.twitter.finagle.samantha.protocol._
import com.twitter.finagle.util.DefaultTimer
import com.twitter.io.Buf
import com.twitter.util.{ Closable, Future, Time, Timer }
import org.jboss.netty.buffer.ChannelBuffer

object Client {
  
  /**
    * Construct a client from a single host.
    * @param host a String of host:port combination.
    */
  def apply(host: String): Client =
    new Client(com.twitter.finagle.Samantha.client.newClient(host))
  
  /**
    * Construct a client from a single Service.
    */
  def apply(raw: ServiceFactory[Command, Feedback]): Client =
    new Client(raw)
}

class Client(
  override val factory: ServiceFactory[Command, Feedback],
  private[samantha] val timer: Timer = DefaultTimer.twitter)
  extends BaseClient(factory)

trait NormalCommands { self: BaseClient =>
}

/**
  * Connects to a single host
  * @param factory: Finagle service factory object built with the Redis codec
  */
abstract class BaseClient(
  protected val factory: ServiceFactory[Command, Feedback])
  extends Closable {
  
  def close(deadline: Time): Future[Unit] = factory.close(deadline)
  
  /**
    * Helper function for passing a command to the service
    */
  private[samantha] def doRequest[T](cmd: Command)(handler: PartialFunction[Feedback, Future[T]]): Future[T] = {
    factory.toService.apply(cmd).flatMap (handler orElse {
      case ErrorFeedback(message)     => Future.exception(ServerError(message))
      case TextualFeedback("QUEUED")  => Future.Done.asInstanceOf[Future[Nothing]]
      case _                          => Future.exception(new IllegalStateException)
    })
  }
}
