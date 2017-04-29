package io.samantha
package clients

import java.net.InetSocketAddress

import com.twitter.concurrent.AsyncQueue
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.io.{IO, Tcp}
import akka.util.ByteString
import io.scalac.amqp.Connection

import scala.concurrent.{Future, Promise}
import Tcp._
import akka.stream.scaladsl.SourceQueueWithComplete
import redis.RedisClient

class NetworkClient(remote: InetSocketAddress, queue: SourceQueueWithComplete[String])
  extends Actor {
  
  import context.system
  import NetworkClient._
  
  IO(Tcp) ! Connect(remote)
  
  //private[this] var redis = RedisClient()
  
  private[this] var reconnects = 0
  
  def receive = {
    case CommandFailed(_: Connect) =>
      println("Connect failed")
      context stop self
    
    case c @ Connected(addr, local) =>
      val connection = sender()
      connection ! Register(self)
      
      context become {
        case CommandFailed(w: Write) =>
          println("Failed to write request")
        case Received(data) => queue.offer(data.decodeString("UTF-8"))
        case CloseConnection => connection ! Close
        case str: String =>
          connection ! Write(ByteString(str))
          Thread.sleep(300)
        case _: ConnectionClosed =>
          println("Connection closed by server")
          context stop self
      }
    case _ => println("Something went wrong")
  }
  
//  def connectedContext = {
//    case Received(data) =>
//    case CloseConnection =>
//    case _: ConnectionClosed =>
//  }
  
}

object NetworkClient {
  
  def props(addr: String, queue: SourceQueueWithComplete[String]) =
    Props(classOf[NetworkClient], new InetSocketAddress(addr, 8102), queue)
  
  sealed trait NetworkCommand
  
  case object CloseConnection extends NetworkCommand
  
  case object Reconnect extends NetworkCommand
  
}

