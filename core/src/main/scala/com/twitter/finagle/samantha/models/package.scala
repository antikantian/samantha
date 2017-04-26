package com.twitter.finagle.samantha

package object models {
  
  sealed trait Device
  
  case class NetworkDevice(addr: String, port: Int) extends Device
  
  type CommandSet = Map[String, String]
  
}