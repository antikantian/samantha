package io.samantha
package devices

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future, Promise}
import akka.actor._
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl._
import akka.util.ByteString
import com.twitter.concurrent.AsyncQueue
import com.twitter.bijection._
import com.twitter
import io.samantha.clients.NetworkClient

//class NetworkDevice(addr: String, port: Int, prefix: Option[String], suffix: Option[String])(implicit AS: ActorSystem) {
//
//  private[this] val connection = Tcp().outgoingConnection(addr, port).runWith()
//
//  private[this] val commandQueue = Source.queue(100, OverflowStrategy.dropHead).keepAlive(30.seconds, "\r").to(Sink.foreach())
//
//  private[this] val feedbackQueue: AsyncQueue[String] = new AsyncQueue[String]()
//
//  val serialize: String => ByteString = s => ByteString(prefix.getOrElse("") + s + suffix.getOrElse(""))
//
//  val deserialize: ByteString => String = _.utf8String.stripLineEnd
//
//  val inFlow: Flow[ByteString, String, _] = Flow fromFunction deserialize
//
//  val outFlow: Flow[String, ByteString, _] = Flow fromFunction serialize
//
//
//  val bufferSize = 100
//
//  //if the buffer fills up this drops the oldest elements when a new element
//  //comes in
//  val overflowStrategy = akka.stream.OverflowStrategy.dropHead
//
//  val queue = Source.queue(bufferSize, overflowStrategy)
//    .filter(!_.raining)
//    .to(Sink foreach println)
//    .run() // in order to "keep" the queue Materialized value instead of the Sink's
//
//  queue offer Weather("02139", 32.0, true)
//
////  private[this] val outFlow =
////    Flow[String]
////      .mapAsync(1)(_ => Bijection[twitter.util.Future[String], scala.concurrent.Future[String]](outgoing.poll()).map(str => s"$str\r"))
//
//
//
//
////  val keepAlive = Flow[String].keepAlive(30.seconds, () => "\r")
////
////  val replParser =
////    Flow[String].takeWhile(_ != "q")
////      .concat(Source.single("BYE"))
////      .map(elem => ByteString(s"$elem\n"))
////
////  def sendCommand(cmd: String)
////
////  def receiveFeedback(f: String)
//
////  def client(): Unit =
////    Tcp()
////      .outgoingConnection(host, port)
////      .join(protocol).async
////      .runWith(prompt("C").async, print) // note .async here
////
////  val outgoing:Flow[Message, ByteString, _] = Flow fromFunction serialize
////
////  val protocol = BidiFlow.fromFlows(incoming, outgoing)
////
////  def prompt(s:String):Source[Message, _] = Source fromIterator {
////    () => Iterator.continually(StdIn readLine s"[$s]> ")
////  }
////
////  val print:Sink[Message, _] = Sink foreach println
////
////  args.headOption foreach {
////    case "server" => server()
////    case "client" => client()
////  }
////
////  def server():Unit =
////    Tcp()
////      .bind(host, port)
////      .runForeach { _
////        .flow
////        .join(protocol)
////        .runWith(prompt("S"), print)
////      }
////
////  def client():Unit =
////    Tcp()
////      .outgoingConnection(host, port)
////      .join(protocol)
////      .runWith(prompt("C"), print)
////}
//
//}
//
