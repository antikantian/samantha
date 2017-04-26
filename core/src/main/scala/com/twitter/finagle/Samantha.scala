package com.twitter.finagle

import java.net.SocketAddress

import com.twitter.finagle
import com.twitter.finagle.client._
import com.twitter.finagle.dispatch.GenSerialClientDispatcher
import com.twitter.finagle.netty4.Netty4Transporter
import com.twitter.finagle.param.{ ExceptionStatsHandler => _, Monitor => _, ResponseClassifier => _, Tracer => _, _ }
import com.twitter.finagle.samantha.Config
import com.twitter.finagle.samantha.network._
import com.twitter.finagle.samantha.protocol._
import com.twitter.finagle.service.{ResponseClassifier, RetryBudget}
import com.twitter.finagle.stats.{ExceptionStatsHandler, StatsReceiver}
import com.twitter.finagle.tracing.Tracer
import com.twitter.finagle.transport.Transport
import com.twitter.io.Buf
import com.twitter.util.{Duration, Monitor}

trait SamanthaRichClient { self: Client[Command, Feedback] =>
  
  def newRichClient(dest: String): samantha.Client =
    samantha.Client(newClient(dest))
  
  def newRichClient(dest: Name, label: String): samantha.Client =
    samantha.Client(newClient(dest, label))
  
}

object Samantha extends Client[Command, Feedback] with SamanthaRichClient {
  
  object Client {
    
    val defaultParams: Stack.Params =
      StackClient.defaultParams + param.ProtocolLibrary("samantha")
    
    val newStack: Stack[ServiceFactory[Command, Feedback]] =
      StackClient.newStack
    
  }
  
  case class Client(
    stack: Stack[ServiceFactory[Command, Feedback]] = Client.newStack,
    params: Stack.Params = Client.defaultParams)
    extends StdStackClient[Command, Feedback, Client]
      with WithDefaultLoadBalancer[Client]
      with SamanthaRichClient {
    
    protected def copy1(
      stack: Stack[ServiceFactory[Command, Feedback]] = this.stack,
      params: Stack.Params = this.params
    ): Client = copy(stack, params)
    
    protected type In = Buf
    protected type Out = Buf
    
    protected def newTransporter(addr: SocketAddress): Transporter[In, Out] =
      Netty4Transporter.framedBuf(None, addr, params)
    
    protected def newDispatcher(transport: Transport[In, Out]): Service[Command, Feedback] = {
      new NetworkDispatcher(new NetworkTransport(transport), Config(params))
    }
    
    def withCommandPrefix(cp: String): Client =
      configured(NetworkConfig.CommandPrefix(Option(cp)))
    
    def withCommandSuffix(cs: String): Client =
      configured(NetworkConfig.CommandSuffix(Option(cs)))
    
    override val withLoadBalancer: DefaultLoadBalancingParams[Client] =
      new DefaultLoadBalancingParams(this)
    
    override val withTransport: ClientTransportParams[Client] =
      new ClientTransportParams(this)
    
    override val withSession: ClientSessionParams[Client] =
      new ClientSessionParams(this)
    
    override val withSessionQualifier: SessionQualificationParams[Client] =
      new SessionQualificationParams(this)
    
    override val withAdmissionControl: ClientAdmissionControlParams[Client] =
      new ClientAdmissionControlParams(this)
    
    override def withLabel(label: String): Client = super.withLabel(label)
    
    override def withStatsReceiver(statsReceiver: StatsReceiver): Client =
      super.withStatsReceiver(statsReceiver)
    
    override def withMonitor(monitor: Monitor): Client = super.withMonitor(monitor)
    
    override def withTracer(tracer: Tracer): Client = super.withTracer(tracer)
    
    override def withExceptionStatsHandler(exceptionStatsHandler: ExceptionStatsHandler): Client =
      super.withExceptionStatsHandler(exceptionStatsHandler)
    
    override def withRequestTimeout(timeout: Duration): Client = super.withRequestTimeout(timeout)
    
    override def withResponseClassifier(responseClassifier: ResponseClassifier): Client =
      super.withResponseClassifier(responseClassifier)
    
    override def withRetryBudget(budget: RetryBudget): Client = super.withRetryBudget(budget)
    
    override def withRetryBackoff(backoff: Stream[Duration]): Client = super.withRetryBackoff(backoff)
    
    override def configured[P](psp: (P, Stack.Param[P])): Client = super.configured(psp)
    
    override def filtered(filter: Filter[Command, Feedback, Command, Feedback]): Client =
      super.filtered(filter)
    
  }
  
  val client: Samantha.Client = Client()
  
  def newClient(dest: Name, label: String): ServiceFactory[Command, Feedback] =
    client.newClient(dest, label)
  
  def newService(dest: Name, label: String): Service[Command, Feedback] =
    client.newService(dest, label)
  
}