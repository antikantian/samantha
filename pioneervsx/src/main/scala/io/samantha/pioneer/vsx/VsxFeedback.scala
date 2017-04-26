package io.samantha.pioneer.vsx

import com.twitter.io.Buf
import io.samantha.protocol.Input
import io.samantha.protocol.Input._

sealed trait VsxMessage

sealed trait VsxCommand extends VsxMessage

sealed trait VsxFeedback extends VsxMessage

object VsxMessage {

  // Commands
  
  case object PowerOn extends VsxCommand
  
  case object PowerOff extends VsxCommand
  
  case object RequestPowerStatus extends VsxCommand
  
  case object VolumeUp extends VsxCommand
  
  case object VolumeDown extends VsxCommand
  
  case class VolumeSet(level: Int) extends VsxCommand
  
  case object RequestVolumeLevel extends VsxCommand
  
  case class InputChange(source: Input) extends VsxCommand
  
  case object InputCycleUp extends VsxCommand
  
  case object InputCycleDown extends VsxCommand
  
  case object RequestInputSource extends VsxCommand
  
  case class SetListeningMode(mode: Int) extends VsxCommand
  
  case object RequestListeningMode extends VsxCommand
  
  case object BassIncrement extends VsxCommand
  
  case object BassDecrement extends VsxCommand
  
  case object RequestBassStatus extends VsxCommand
  
  case object TrebleIncrement extends VsxCommand
  
  case object TrebleDecrement extends VsxCommand
  
  case object RequestTrebleStatus extends VsxCommand
  
  case class RequestHdmiAudioStatus extends VsxCommand
  
   
  
  
  // Feedback
  
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
        str.diff("SR").toInt match {
          case 1 | 9  => ListeningMode("Stereo")
          case 151    => ListeningMode("Auto Level Control")
          case 3      => ListeningMode("Front Stage Surround Advance Focus")
          case 4      => ListeningMode("Front Stage Surround Advance Wide")
          case 10     => ListeningMode("Standard")
          case 11     => ListeningMode("2ch source")
          case 13     => ListeningMode("Pro Logic 2 Movie")
          case 18     => ListeningMode("Pro Logic 2x Movie")
          case _      => ListeningMode("Unknown")
        }
      case str if str.startsWith("HO")    =>
        str.diff("HO").toInt match {
          case 0 => HdmiOutput("HDMI ALL")
          case 1 => HdmiOutput("HDMI OUT 1")
          case 2 => HdmiOutput("HDMI OUT 2")
          case 9 => HdmiOutput("HDMI Out (cyclic)")
          case _ => HdmiOutput("Unknown")
        }
      case str if str.startsWith("BA")    =>
        str.diff("BA").toInt match {
          case 0  => Bass(6)
          case 1  => Bass(5)
          case 2  => Bass(4)
          case 3  => Bass(3)
          case 4  => Bass(2)
          case 5  => Bass(1)
          case 6  => Bass(0)
          case 7  => Bass(-1)
          case 8  => Bass(-2)
          case 9  => Bass(-3)
          case 10 => Bass(-4)
          case 11 => Bass(-5)
          case 12 => Bass(-6)
        }
      case str if str.startsWith("TR")    =>
        str.diff("TR").toInt match {
          case 0  => Treble(6)
          case 1  => Treble(5)
          case 2  => Treble(4)
          case 3  => Treble(3)
          case 4  => Treble(2)
          case 5  => Treble(1)
          case 6  => Treble(0)
          case 7  => Treble(-1)
          case 8  => Treble(-2)
          case 9  => Treble(-3)
          case 10 => Treble(-4)
          case 11 => Treble(-5)
          case 12 => Treble(-6)
        }
      case str if str.startsWith("APR")   =>
        str.diff("APR").toInt match {
          case 0 => Zone2Power(true)
          case _ => Zone2Power(false)
        }
      case str if str.startsWith("ZV")    =>
        Zone2Volume(str.diff("ZV").toInt)
      case str if str.startsWith("Z2MUT") =>
        str.diff("Z2MUT").toInt match {
          case 0 => Zone2Mute(true)
          case _ => Zone2Mute(false)
        }
      case str if str.startsWith("Z2F")   =>
        str.diff("Z2F").toInt match {
          case 4  => Zone2Source(DVD)
          case 5  => Zone2Source(TV)
          case 15 => Zone2Source(DVR)
          case 10 => Zone2Source(Video1)
          case 14 => Zone2Source(Video2)
          case 26 => Zone2Source(InternetRadio)
          case 1  => Zone2Source(CD)
          case 2  => Zone2Source(CDR)
          case _  => Zone2Source(UnknownSource)
      case str if str.startsWith("FL")    =>
        Display(str.diff("FL"))
      case str if str.startsWith("RGB")   =>
        InputName(str.diff("RGB").drop(2))
      case _                              =>
        UnknownFeedback
    }
  }
}