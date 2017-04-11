package com.twitter.finagle
package samantha.feedback

import com.twitter.concurrent.Broker
import com.twitter.finagle.samantha.protocol._
import com.twitter.finagle.transport.Transport
import com.twitter.util.{Future, Promise, Time}

import scala.util.control.NonFatal

class NetworkDispatcher(trans: Transport[Command, Feedback])
  extends Service[Command, Feedback] {
  
  private[this] val inbound = new Broker[Feedback]
  
  private[this] def readLoop(): Future[Unit] = {
    trans.read() flatMap { fb =>
      println(s"Inbound: $fb")
      inbound ! fb
    } before readLoop()
    //trans.read().flatMap(inbound ! _) before readLoop()
  }
  
  readLoop() onFailure { _ => close() }
  
  protected def dispatch(req: Command, p: Promise[Feedback]): Future[Unit] = {
    trans.write(req)
      .onSuccess { _ => p.setValue(NoFeedback) }
      .onFailure { case NonFatal(ex) => p.setException(ex) }
  }
  
//  private[this] def writeLoop(out: Offer[Command]): Future[Unit] = {
//    out.sync.flatMap(trans.write) before writeLoop(out)
//  }
//
//  protected def loop(out: Offer[Command]): Unit = {
//    writeLoop(out) onFailure { _ => close() }
//    readLoop() onFailure { _ => close() }
//  }
  
  override def apply(req: Command): Future[Feedback] = {
    println(s"Outbound: $req")
    trans.write(req) before trans.read()
  }
  
  override def close(deadline: Time): Future[Unit] = {
    trans.close(deadline)
  }
}
