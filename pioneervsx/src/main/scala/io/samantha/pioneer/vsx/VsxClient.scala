package io.samantha.pioneer.vsx

import java.net.InetSocketAddress

import com.twitter.concurrent.AsyncQueue
import akka.actor.{Actor, ActorSystem, Props}
import akka.io.{IO, Tcp}
import akka.util.ByteString

import scala.concurrent.Promise
import Tcp._

class VsxClient(remote: InetSocketAddress, queue: AsyncQueue[VsxFeedback]) extends Actor {
  import context.system
  
  println("Connecting")
  IO(Tcp) ! Connect(remote)
  
  def receive = {
    case CommandFailed(_: Connect) =>
      println("Connect failed")
      context stop self

    case c @ Connected(addr, local) =>
      println("Connected")
      val connection = sender()
      connection ! Register(self)
      println("Sending request")
      connection ! Write(ByteString("\r"))
      
      context become {
        case CommandFailed(w: Write) =>
          println("Failed to write request")
        case Received(data) =>
          print("Received response: ")
          println(VsxMessage.decode(data))
        case "close" =>
          println("Closing connection")
          connection ! Close
        case msg: VsxCommand =>
          connection ! Write(msg.toByteString ++ ByteString("\r"))
        case _: ConnectionClosed =>
          println("Connection closed by server")
          context stop self
      }
    case _ => println("Something went wrong")
  }
}

object VsxClient {
  
  def apply(q: AsyncQueue[VsxFeedback])(implicit sys: ActorSystem) =
    sys.actorOf(Props(classOf[VsxClient], q))
  
}