package io.samantha
package pioneer.vsx

import akka.util.ByteString
import com.twitter.io.Buf
import com.twitter.bijection.AbstractBijection
import io.samantha.protocol.Input
import io.samantha.protocol.Input._

sealed trait VsxMessage

sealed trait VsxCommand extends VsxMessage {
  def raw: String
  
  def toBuf: Buf = Buf.Utf8(raw)
  
  def toByteString: ByteString = ByteString(raw)
}

sealed trait VsxFeedback extends VsxMessage

object VsxCommand {
  
  case object PowerOn extends VsxCommand {
    def raw = "PO"
  }
  
  case object PowerOff extends VsxCommand {
    def raw = "PF"
  }
  
  case object RequestPowerStatus extends VsxCommand {
    def raw = "?P"
  }
  
  case object VolumeUp extends VsxCommand {
    def raw = "VU"
  }
  
  case object VolumeDown extends VsxCommand {
    def raw = "VD"
  }
  
  case class VolumeSet(level: Int) extends VsxCommand {
    def raw = {
      level match {
        case v if v > 0                => "000VL"
        case v if v >= 0 && v <= 9     => s"00${v}VL"
        case v if v >= 10 && v <= 99   => s"0${v}VL"
        case v if v >= 100 && v <= 185 => s"${v}VL"
        case _                         => "185VL"
      }
    }
  }
  
  case object RequestVolumeLevel extends VsxCommand {
    def raw = "?V"
  }
  
  case object MuteOn extends VsxCommand {
    def raw = "MO"
  }
  
  case object MuteOff extends VsxCommand {
    def raw = "MF"
  }
  
  case object RequestMuteStatus extends VsxCommand {
    def raw = "?M"
  }
  
  case class InputChange(source: Input) extends VsxCommand {
    def raw = source match {
      case DVD           => "04FN"
      case BluRay        => "25FN"
      case TV            => "05FN"
      case DVR           => "15FN"
      case Video1        => "10FN"
      case Video2        => "14FN"
      case HDMI1         => "19FN"
      case HDMI2         => "20FN"
      case HDMI3         => "21FN"
      case HDMI4         => "22FN"
      case HDMI5         => "23FN"
      case InternetRadio => "26"
      case CD            => "01FN"
      case CDR           => "03FN"
      case Tuner         => "02FN"
      case Phono         => "00FN"
      case _             => "19FN"
    }
  }
  
  case object InputCycleUp extends VsxCommand {
    def raw = "FU"
  }
  
  case object InputCycleDown extends VsxCommand {
    def raw = "FD"
  }
  
  case object RequestInputSource extends VsxCommand {
    def raw = "?F"
  }
  
  case class SetListeningMode(mode: Int) extends VsxCommand {
    def raw = mode.toString match {
      case str if str.length == 4 => str
      case str if str.length == 3 => s"0$str"
      case str if str.length == 2 => s"00$str"
      case str if str.length == 1 => s"000$str"
    }
  }
  
  case object RequestListeningMode extends VsxCommand {
    def raw = "?S"
  }
  
  case object BassIncrement extends VsxCommand {
    def raw = "BI"
  }
  
  case object BassDecrement extends VsxCommand {
    def raw = "BD"
  }
  
  case object RequestBassStatus extends VsxCommand {
    def raw = "?BA"
  }
  
  case object TrebleIncrement extends VsxCommand {
    def raw = "TI"
  }
  
  case object TrebleDecrement extends VsxCommand {
    def raw = "TD"
  }
  
  case object RequestTrebleStatus extends VsxCommand {
    def raw = "?TR"
  }
  
  case object RequestHdmiAudioStatus extends VsxCommand {
    def raw = "?HA"
  }
  
}

object VsxFeedback {
  
  case class Power(state: Boolean) extends VsxFeedback
  
  case class Volume(level: Int) extends VsxFeedback
  
  case class Mute(state: Boolean) extends VsxFeedback
  
  case class InputSource(source: Input) extends VsxFeedback
  
  case class ListeningModeSet(mode: Int) extends VsxFeedback
  
  case class ListeningMode(mode: String) extends VsxFeedback
  
  case class HdmiOutput(source: String) extends VsxFeedback
  
  case class Bass(level: Int) extends VsxFeedback
  
  case class Treble(level: Int) extends VsxFeedback
  
  case class Zone2Power(state: Boolean) extends VsxFeedback
  
  case class Zone2Volume(level: Int) extends VsxFeedback
  
  case class Zone2Mute(state: Boolean) extends VsxFeedback
  
  case class Zone2Input(source: Input) extends VsxFeedback
  
  case class Display(text: String) extends VsxFeedback
  
  case class InputName(text: String) extends VsxFeedback
  
  case object KeepAlive extends VsxFeedback
  
  case object UnknownFeedback extends VsxFeedback
  
}

object VsxMessage {
  
  import VsxFeedback._
  
  def encode(msg: VsxCommand): String = {
    msg.raw
  }
  
  def decode(buf: Buf): VsxFeedback = Buf.Utf8.unapply(buf).map(decode).get
  
  def decode(bs: ByteString): VsxFeedback = decode(bs.decodeString("UTF-8"))
  
  def decode(resp: String): VsxFeedback = {
    resp.stripLineEnd match {
      case str if str.startsWith("PWR") =>
        str.diff("PWR") match {
          case "0" => Power(true)
          case _   => Power(false)
        }
      case str if str.startsWith("VOL") =>
        Volume(str.diff("VOL").toInt)
      case str if str.startsWith("MUT") =>
        str.diff("MUT") match {
          case "0" => Mute(true)
          case _   => Mute(false)
        }
      case str if str.startsWith("FN") =>
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
      case str if str.startsWith("SR") =>
        str.diff("SR").toInt match {
          case 1 | 9 => ListeningMode("Stereo")
          case 151   => ListeningMode("Auto Level Control")
          case 3     => ListeningMode("Front Stage Surround Advance Focus")
          case 4     => ListeningMode("Front Stage Surround Advance Wide")
          case 10    => ListeningMode("Standard")
          case 11    => ListeningMode("2ch source")
          case 13    => ListeningMode("Pro Logic 2 Movie")
          case 18    => ListeningMode("Pro Logic 2x Movie")
          case _     => ListeningMode("Unknown")
        }
      case str if str.startsWith("HO") =>
        str.diff("HO").toInt match {
          case 0 => HdmiOutput("HDMI ALL")
          case 1 => HdmiOutput("HDMI OUT 1")
          case 2 => HdmiOutput("HDMI OUT 2")
          case 9 => HdmiOutput("HDMI Out (cyclic)")
          case _ => HdmiOutput("Unknown")
        }
      case str if str.startsWith("BA") =>
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
      case str if str.startsWith("TR") =>
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
      case str if str.startsWith("APR") =>
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
      case str if str.startsWith("Z2F") =>
        str.diff("Z2F").toInt match {
          case 4  => Zone2Input(DVD)
          case 5  => Zone2Input(TV)
          case 15 => Zone2Input(DVR)
          case 10 => Zone2Input(Video1)
          case 14 => Zone2Input(Video2)
          case 26 => Zone2Input(InternetRadio)
          case 1  => Zone2Input(CD)
          case 2  => Zone2Input(CDR)
          case _  => Zone2Input(UnknownSource)
        }
      case str if str.startsWith("FL")  =>
        Display(str.diff("FL"))
      case str if str.startsWith("RGB") =>
        InputName(str.diff("RGB").drop(2))
      case "\r" =>
        KeepAlive
      case _                            =>
        UnknownFeedback
    }
  }
}

trait CodecInstances {
  implicit val command2String =
    new Encodable[VsxCommand, String] {
      def encode(cmd: VsxCommand): String = cmd.raw + "\r"
    }
  
  implicit val string2Feedback =
    new Decodable[String, VsxFeedback] {
      def decode(str: String): VsxFeedback = VsxMessage.decode(str)
    }
}

