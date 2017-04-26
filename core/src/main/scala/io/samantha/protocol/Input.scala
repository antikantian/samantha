package io.samantha
package protocol

sealed trait Input

object Input {
  case object DVD extends Input
  case object BluRay extends Input
  case object TV extends Input
  case object DVR extends Input
  case object Video1 extends Input
  case object Video2 extends Input
  case object HDMI1 extends Input
  case object HDMI2 extends Input
  case object HDMI3 extends Input
  case object HDMI4 extends Input
  case object HDMI5 extends Input
  case object InternetRadio extends Input
  case object CD extends Input
  case object CDR extends Input
  case object Phono extends Input
  case object Tuner extends Input
  case object Alexa extends Input
  case object RaspberryPi extends Input
  case object Kodi extends Input
  case object AppleTV extends Input
  case object FireTV extends Input
  case object Netflix extends Input
  case object SAT extends Input
  case object Sky extends Input
  case object UnknownSource extends Input
}

