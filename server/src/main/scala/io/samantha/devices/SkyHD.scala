package io.samantha
package devices

import java.util.concurrent.atomic.AtomicReference

class SkyHD(gateway: String) {
  
  import SkyHD._
  
  private[this] val currentState = new AtomicReference[Status]
  
  
  
}

object SkyHD {
  
  case class Status(power: Boolean, channel: Int)
  
}

trait SkyProtocol {
  
  sealed trait SkyCommand {
    def raw: Array[Byte]
  }
}