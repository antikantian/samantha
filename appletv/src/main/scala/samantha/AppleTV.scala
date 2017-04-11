package samantha

import com.twitter.finagle._
import com.twitter.finagle.mdns._
import com.twitter.util._

object AppleTV extends AppleTVThings {
  
  def findDevices: Var[Addr] =
    new MDNSResolver().bind("_appletv-v2._tcp.local.")
  
}

sealed abstract class AppleTVThings {
  
  case class Device(
    name: String,
    address: String,
    loginId: String,
    port: Int = 3689
  )
  
}