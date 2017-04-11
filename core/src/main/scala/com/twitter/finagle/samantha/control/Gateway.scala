//package com.twitter.finagle.samantha.control
//
//import java.net.InetSocketAddress
//
//import com.twitter.finagle.{ Client, Name, Server, ServiceFactory }
//import samantha.client._
//import samantha.server._
//
//sealed trait Gateway {
//  def name: Name
//  def host: String
//  def port: Int
//}
//
//object Gateway {
//
//  case class Network(
//      devices : Vector[Device],
//      name    : Name,
//      host    : String,
//      port    : Int,
//      user    : Option[String] = None,
//      pass    : Option[String] = None
//  ) extends Gateway {
//
//    def toSocketAddress: InetSocketAddress = new InetSocketAddress(host, port)
//
//    def getIPCommand(device: String, cmd: String): Option[String] =
//      for {
//        dev <- devices.find(_.name == device)
//        code <- dev.ipCodes.find(_.name == cmd)
//      } yield code.data
//
//  }
//
//}