package io.samantha.pioneer.vsx

import com.twitter.io.Buf
import io.samantha.protocol.Input
import io.samantha.protocol.Input._

sealed trait VsxFeedback

object VsxFeedback {
  
  case class Power(state: Boolean) extends VsxFeedback
  
  case class Volume(level: Int) extends VsxFeedback
  
  case class Mute(state: Boolean) extends VsxFeedback
  
  case class InputSource(source: Input) extends VsxFeedback
  
  case class ListeningModeSet(mode: Int) extends VsxFeedback
  
  case class ListeningMode(mode: Int) extends VsxFeedback
  
  case class HdmiOutput(source: Int) extends VsxFeedback
  
  case class Bass(level: Int) extends VsxFeedback
  
  case class Treble(level: Int) extends VsxFeedback
  
  case class Zone2Power(state: Boolean) extends VsxFeedback
  
  case class Zone2Volume(level: Int) extends VsxFeedback
  
  case class Zone2Input(source: Int) extends VsxFeedback
  
  case class Display(text: String) extends VsxFeedback
  
  case class InputName(text: String) extends VsxFeedback
  
  case object UnknownFeedback extends VsxFeedback
  
  def encode(msg: VsxFeedback): Buf = {
  
  }
  
  def decode(buf: Buf): VsxFeedback = {
    Buf.Utf8.unapply(buf).map(_.stripLineEnd) collect {
      case str if str.startsWith("PWR")   =>
        str.diff("PWR") match {
          case "0" => Power(true)
          case _   => Power(false)
        }
      case str if str.startsWith("VOL")   =>
        Volume(str.diff("VOL").toInt)
      case str if str.startsWith("MUT")   =>
        str.diff("MUT") match {
          case "0" => Mute(true)
          case _   => Mute(false)
        }
      case str if str.startsWith("FN")    =>
        str.diff("FN").toInt match {
          case 4  => InputSource(DVD)
          case 25 => InputSource(BluRay)
          case 5  => InputSource(TV)
          case 15 => InputSource(DVR)
          case 10 => InputSource(Video1)
          case 14 => InputSource(Video2)
          case 19 => InputSource(HDMI1)
          case 20 => InputSource(HDMI2)
          case 21 => InputSource(HDMI3)
          case 22 => InputSource(HDMI4)
          case 23 => InputSource(HDMI5)
          case 26 => InputSource(InternetRadio)
          case 1  => InputSource(CD)
          case 3  => InputSource(CDR)
          case 2  => InputSource(Tuner)
          case 0  => InputSource(Phono)
          case _  => InputSource(UnknownSource)
        }
      case str if str.startsWith("SR")    =>
      case str if str.startsWith("LM")    =>
      case str if str.startsWith("HO")    =>
      case str if str.startsWith("BA")    =>
      case str if str.startsWith("TR")    =>
      case str if str.startsWith("APR")   =>
      case str if str.startsWith("ZV")    =>
      case str if str.startsWith("Z2MUT") =>
      case str if str.startsWith("Z2F")   =>
      case str if str.startsWith("FL")    =>
      case str if str.startsWith("RGB")   =>
      case _                              =>
    }
  }
}