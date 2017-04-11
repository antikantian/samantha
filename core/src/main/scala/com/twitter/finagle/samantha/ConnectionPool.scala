package com.twitter.finagle.samantha

import com.twitter.finagle.{ ClientConnection, Service, ServiceFactory, Stack, Stackable }
import com.twitter.finagle.dispatch.PipeliningDispatcher
import com.twitter.finagle.param.Stats
import com.twitter.finagle.pool.SingletonPool
import com.twitter.finagle.samantha.protocol._
import com.twitter.finagle.stats.StatsReceiver
import com.twitter.finagle.util.DefaultTimer
import com.twitter.finagle.transport.Transport
import com.twitter.util.{ Future, Local, Time }

object ConnectionPool {
  
  private sealed trait UseFor
  private case object Network extends UseFor
  private case object Http extends UseFor
  
  private val useFor = new Local[UseFor]
  
  def forNetwork[T](factory: ServiceFactory[Command, Feedback]): Future[Service[Command, Feedback]] =
    useFor.let(Network)(factory())
  
  def forHttp[T](factory: ServiceFactory[Command, Feedback])(cmd: Command): Future[Feedback] =
    useFor.let(Http)(factory.toService(cmd))
  
  def newDispatcher[T](
    transport: Transport[Command, Feedback],
    statsReceiver: StatsReceiver): Service[Command, Feedback] =
    useFor() match {
      case Some(Network) => new PipeliningDispatcher(transport, statsReceiver, DefaultTimer.twitter)
      case _             => new PipeliningDispatcher(transport, statsReceiver, DefaultTimer.twitter)
    }
  
  def module: Stackable[ServiceFactory[Command, Feedback]] =
    new Stack.Module1[Stats, ServiceFactory[Command, Feedback]] {
      val role = Stack.Role("Connection pool")
      val description = "Manage Samanthas connections"
      def make(_stats: Stats, next: ServiceFactory[Command, Feedback]) = {
        val Stats(sr) = _stats
        new ConnectionPool(next, sr)
      }
    }
}

class ConnectionPool(
    underlying: ServiceFactory[Command, Feedback],
    statsReceiver: StatsReceiver)
  extends ServiceFactory[Command, Feedback] {
  
  private[this] val singletonPool =
    new SingletonPool(underlying, statsReceiver.scope("singletonpool"))
  
  private[this] val networkPool =
    new SingletonPool(underlying, statsReceiver.scope("networkpool"))
  
  final def apply(conn: ClientConnection): Future[Service[Command, Feedback]] = {
    ConnectionPool.useFor() match {
      case Some(ConnectionPool.Http) =>
        underlying(conn)
      case Some(ConnectionPool.Network) =>
        networkPool(conn)
      case None =>
        singletonPool(conn)
    }
  }
  
  final def close(deadline: Time): Future[Unit] = {
    singletonPool.close(deadline) before networkPool.close(deadline)
  }
}