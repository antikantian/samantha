package com.twitter.finagle
package samantha

import com.twitter.io.Buf
import com.twitter.finagle.util.DefaultTimer
import com.twitter.util.{Closable, Future, Time, Timer}

object Client {
  
  def apply(host: String): Client =
    new Client(com.twitter.finagle.Samantha.client.newClient(host))
  
  def apply(raw: ServiceFactory[Buf, Buf]): Client =
    new Client(raw)
  
}

class Client(
  override val factory: ServiceFactory[Buf, Buf],
  private[samantha] val timer: Timer = DefaultTimer.twitter)
  extends BaseClient(factory)

abstract class BaseClient(
  protected val factory: ServiceFactory[Buf, Buf])
  extends Closable {
  
  def close(deadline: Time): Future[Unit] = factory.close(deadline)
  
}
