package io.samantha
package devices

import java.util.concurrent.atomic.AtomicReference

import scala.concurrent.duration._
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl._
import akka.util.ByteString
import redis.RedisClient
import com.twitter.io.Buf
import com.twitter.util.Time
import com.twitter.bijection.AbstractBijection
import io.samantha.clients.NetworkClient
import io.samantha.protocol.Input
import io.samantha.protocol.Input._

class PioneerVSX(addr: String)(implicit A: ActorSystem, M: ActorMaterializer) extends NetworkDevice {
  
  import PioneerVSX._
  
  private val redis = RedisClient("raspberrypi.local")
  
  private val stream =
    Source.queue[VsxCommand](0, OverflowStrategy.dropHead)
      .via(Flow fromFunction serialize)
      .via(Flow[ByteString].keepAlive(15.seconds, () => ByteString("\r")))
      .via(Tcp().outgoingConnection(addr, 8102))
      .via(Framing.delimiter(ByteString("\r\n"), maximumFrameLength = 256, allowTruncation = true))
      .via(Flow fromFunction deserialize)
      .to(Sink foreach update)
      .run()
  
  private val currentState = new AtomicReference[ReceiverStatus](ReceiverStatus())
  
  def setPower(b: Boolean): Unit =
    if (b) {
      send(Command.PowerOn)
    } else {
      send(Command.PowerOff)
    }
  
  def volumeUp(): Unit = send(Command.VolumeUp)
  
  def volumeDown(): Unit = send(Command.VolumeDown)
  
  def setVolume(x: Int): Unit = send(Command.VolumeSet(x))
  
  def setMute(b: Boolean): Unit =
    if (b) {
      send(Command.MuteOn)
    } else {
      send(Command.MuteOff)
    }
  
  def setHdmi1(): Unit = send(Command.InputChange(HDMI1))
  
  def setHdmi2(): Unit = send(Command.InputChange(HDMI2))
  
  def setCD(): Unit = send(Command.InputChange(CD))
  
  def inputUp(): Unit = send(Command.InputCycleUp)
  
  def inputDown(): Unit = send(Command.InputCycleDown)
  
  def setInternetRadio(): Unit = send(Command.InputChange(InternetRadio))
  
  def status: ReceiverStatus = currentState.get
  
  def lastFeedback = Time.now.sinceEpoch.inLongSeconds - status.lastSeen
  
  def send(cmd: VsxCommand): Unit = stream.offer(cmd)
  
  def refreshStatus(): Unit = stream.offer(Command.UpdateStatus)
  
  def update(feedback: VsxFeedback): Unit = {
    println(feedback)
    val cs = currentState.get()
    val ns = feedback match {
      case Feedback.Power(x)       => redis.set("livingroom:pioneervsx:power", x.toString); cs.updatePower(x)
      case Feedback.Volume(x)      => redis.set("livingroom:pioneervsx:volume", x.toString); cs.updateVolume(x)
      case Feedback.Mute(x)        => redis.set("livingroom:pioneervsx:mute", x.toString); cs.updateMute(x)
      case Feedback.InputSource(x) => redis.set("livingroom:pioneervsx:input", x.toString); cs.updateInput(x)
      case Feedback.Bass(x)        => redis.set("livingroom:pioneervsx:bass", x.toString); cs.updateBass(x)
      case Feedback.Treble(x)      => redis.set("livingroom:pioneervsx:treble", x.toString); cs.updateTreble(x)
      case Feedback.Zone2Power(x)  => redis.set("livingroom:pioneervsx:z2power", x.toString); cs.updateZone2Power(x)
      case Feedback.Zone2Volume(x) => redis.set("livingroom:pioneervsx:z2volume", x.toString); cs.updateZone2Volume(x)
      case Feedback.Zone2Mute(x)   => redis.set("livingroom:pioneervsx:z2mute", x.toString); cs.updateZone2Mute(x)
      case Feedback.Zone2Input(x)  => redis.set("livingroom:pioneervsx:z2input", x.toString); cs.updateZone2Input(x)
      case Feedback.Display(x)     => redis.set("livingroom:pioneervsx:display", x.toString); cs.updateDisplayText(x)
      case Feedback.KeepAlive      => cs.updateLastSeen()
      case _                       => cs
    }
    currentState.set(ns)
  }
  
}

object PioneerVSX extends VSXProtocol {
  
  def apply(addr: String)(implicit A: ActorSystem, M: ActorMaterializer) = new PioneerVSX(addr)
  
  case class ReceiverStatus(
    power: Boolean = false,
    volume: Int = 0,
    mute: Boolean = false,
    input: Input = UnknownSource,
    bass: Int = 0,
    treble: Int = 0,
    zone2Power: Boolean = false,
    zone2Volume: Int = 0,
    zone2Mute: Boolean = false,
    zone2Input : Input = UnknownSource,
    displayText: String = "",
    lastSeen   : Long = Time.epoch.inLongSeconds) {
    self =>
    
    def updatePower(x: Boolean): ReceiverStatus = self.copy(power = x)
    
    def updateVolume(x: Int): ReceiverStatus = self.copy(volume = x)
    
    def updateMute(x: Boolean): ReceiverStatus = self.copy(mute = x)
    
    def updateInput(x: Input): ReceiverStatus = self.copy(input = x)
    
    def updateBass(x: Int): ReceiverStatus = self.copy(bass = x)
    
    def updateTreble(x: Int): ReceiverStatus = self.copy(treble = x)
    
    def updateZone2Power(x: Boolean): ReceiverStatus = self.copy(zone2Power = x)
    
    def updateZone2Volume(x: Int): ReceiverStatus = self.copy(zone2Volume = x)
    
    def updateZone2Mute(x: Boolean): ReceiverStatus = self.copy(zone2Mute = x)
    
    def updateZone2Input(x: Input): ReceiverStatus = self.copy(zone2Input = x)
    
    def updateDisplayText(x: String): ReceiverStatus = self.copy(displayText = x)
    
    def updateLastSeen(): ReceiverStatus = self.copy(lastSeen = Time.now.sinceEpoch.inSeconds)
    
  }
  
}

trait VSXProtocol {
  
  sealed trait VsxMessage
  
  sealed trait VsxCommand extends VsxMessage {
    def raw: String
    
    def toBuf: Buf = Buf.Utf8(raw)
    
    def toByteString: ByteString = ByteString(raw)
  }
  
  sealed trait VsxFeedback extends VsxMessage {
    def id: String
  }
  
  object Command {
    
    case object KeepAlive extends VsxCommand {
      def raw = "\r"
    }
    
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
          case v if v < 0                => "000VL"
          case v if v >= 0 && v <= 9     => s"00${v}VL"
          case v if v >= 10 && v <= 99   => s"0${v}VL"
          case v if v >= 100 && v <= 185 => s"${v}VL"
          case _                         => "160VL"
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
        case InternetRadio => "26FN"
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
    
    case object Zone2PowerStatus extends VsxCommand {
      def raw = "?AP"
    }
    
    case object Zone2InputStatus extends VsxCommand {
      def raw = "?ZS"
    }
    
    case object Zone2VolumeStatus extends VsxCommand {
      def raw = "?ZV"
    }
    
    case object Zone2MuteStatus extends VsxCommand {
      def raw = "?Z2M"
    }
    
    case object UpdateStatus extends VsxCommand {
      def raw = "?P\r?V\r?M\r?F\r?S\r?L\r?TR\r?TO\r?SPK\r?HA\r?PQ\r?PKL\r?RML\r?AP\r?ZS\r?ZV\r?Z2M\r?FR\r?FL\r?AST\r?VST\r?RGB"
    }
    
  }
  
  object Feedback {
    
    case class Power(state: Boolean) extends VsxFeedback {
      def id = "power"
    }
    
    case class Volume(level: Int) extends VsxFeedback {
      def id = "volume"
      def toDecibels = (level / 2) - 80.5
    }
    
    case class Mute(state: Boolean) extends VsxFeedback {
      def id = "mute"
    }
    
    case class InputSource(source: Input) extends VsxFeedback {
      def id = "source"
    }
    
    case class ListeningModeSet(mode: Int) extends VsxFeedback {
      def id = "listening_mode_set"
    }
    
    case class ListeningMode(mode: String) extends VsxFeedback {
      def id = "listening_mode"
    }
    
    case class HdmiOutput(source: String) extends VsxFeedback {
      def id = "hdmi_output"
    }
    
    case class Bass(level: Int) extends VsxFeedback {
      def id = "bass"
    }
    
    case class Treble(level: Int) extends VsxFeedback {
      def id = "treble"
    }
    
    case class Zone2Power(state: Boolean) extends VsxFeedback {
      def id = "z2power"
    }
    
    case class Zone2Volume(level: Int) extends VsxFeedback {
      def id = "z2volume"
    }
    
    case class Zone2Mute(state: Boolean) extends VsxFeedback {
      def id = "z2mute"
    }
    
    case class Zone2Input(source: Input) extends VsxFeedback {
      def id = "z2input"
    }
    
    case class Display(text: String) extends VsxFeedback {
      def id = "display"
    }
    
    case class InputName(text: String) extends VsxFeedback {
      def id = "input_name"
    }
    
    case object KeepAlive extends VsxFeedback {
      def id = "keep_alive"
    }
    
    case class UnknownFeedback(x: String) extends VsxFeedback {
      def id = "unknown_feedback"
    }
    
  }
  
  def serialize(cmd: VsxCommand): ByteString = ByteString(encode(cmd) + "\r")
  
  def deserialize(bs: ByteString): VsxFeedback = decode(bs.utf8String)
  
  def encode(msg: VsxCommand): String = {
    msg.raw
  }
  
  def decode(buf: Buf): VsxFeedback = Buf.Utf8.unapply(buf).map(decode).get
  
  def decode(bs: ByteString): VsxFeedback = decode(bs.decodeString("UTF-8"))
  
  def decode(resp: String): VsxFeedback = {
    resp.stripLineEnd match {
      case str if str.startsWith("PWR")   =>
        str.diff("PWR") match {
          case "0" => Feedback.Power(true)
          case _   => Feedback.Power(false)
        }
      case str if str.startsWith("VOL")   =>
        Feedback.Volume(str.diff("VOL").toInt)
      case str if str.startsWith("MUT")   =>
        str.diff("MUT") match {
          case "0" => Feedback.Mute(true)
          case _   => Feedback.Mute(false)
        }
      case str if str.startsWith("FN")    =>
        str.diff("FN").toInt match {
          case 4  => Feedback.InputSource(DVD)
          case 25 => Feedback.InputSource(BluRay)
          case 5  => Feedback.InputSource(TV)
          case 15 => Feedback.InputSource(DVR)
          case 10 => Feedback.InputSource(Video1)
          case 14 => Feedback.InputSource(Video2)
          case 19 => Feedback.InputSource(HDMI1)
          case 20 => Feedback.InputSource(HDMI2)
          case 21 => Feedback.InputSource(HDMI3)
          case 22 => Feedback.InputSource(HDMI4)
          case 23 => Feedback.InputSource(HDMI5)
          case 26 => Feedback.InputSource(InternetRadio)
          case 1  => Feedback.InputSource(CD)
          case 3  => Feedback.InputSource(CDR)
          case 2  => Feedback.InputSource(Tuner)
          case 0  => Feedback.InputSource(Phono)
          case _  => Feedback.InputSource(UnknownSource)
        }
      case str if str.startsWith("SR")    =>
        str.diff("SR").toInt match {
          case 1 | 9 => Feedback.ListeningMode("Stereo")
          case 151   => Feedback.ListeningMode("Auto Level Control")
          case 3     => Feedback.ListeningMode("Front Stage Surround Advance Focus")
          case 4     => Feedback.ListeningMode("Front Stage Surround Advance Wide")
          case 10    => Feedback.ListeningMode("Standard")
          case 11    => Feedback.ListeningMode("2ch source")
          case 13    => Feedback.ListeningMode("Pro Logic 2 Movie")
          case 18    => Feedback.ListeningMode("Pro Logic 2x Movie")
          case _     => Feedback.ListeningMode("Unknown")
        }
      case str if str.startsWith("HO")    =>
        str.diff("HO").toInt match {
          case 0 => Feedback.HdmiOutput("HDMI ALL")
          case 1 => Feedback.HdmiOutput("HDMI OUT 1")
          case 2 => Feedback.HdmiOutput("HDMI OUT 2")
          case 9 => Feedback.HdmiOutput("HDMI Out (cyclic)")
          case _ => Feedback.HdmiOutput("Unknown")
        }
      case str if str.startsWith("BA")    =>
        str.diff("BA").toInt match {
          case 0  => Feedback.Bass(6)
          case 1  => Feedback.Bass(5)
          case 2  => Feedback.Bass(4)
          case 3  => Feedback.Bass(3)
          case 4  => Feedback.Bass(2)
          case 5  => Feedback.Bass(1)
          case 6  => Feedback.Bass(0)
          case 7  => Feedback.Bass(-1)
          case 8  => Feedback.Bass(-2)
          case 9  => Feedback.Bass(-3)
          case 10 => Feedback.Bass(-4)
          case 11 => Feedback.Bass(-5)
          case 12 => Feedback.Bass(-6)
        }
      case str if str.startsWith("TR")    =>
        str.diff("TR").toInt match {
          case 0  => Feedback.Treble(6)
          case 1  => Feedback.Treble(5)
          case 2  => Feedback.Treble(4)
          case 3  => Feedback.Treble(3)
          case 4  => Feedback.Treble(2)
          case 5  => Feedback.Treble(1)
          case 6  => Feedback.Treble(0)
          case 7  => Feedback.Treble(-1)
          case 8  => Feedback.Treble(-2)
          case 9  => Feedback.Treble(-3)
          case 10 => Feedback.Treble(-4)
          case 11 => Feedback.Treble(-5)
          case 12 => Feedback.Treble(-6)
        }
      case str if str.startsWith("APR")   =>
        str.diff("APR").toInt match {
          case 0 => Feedback.Zone2Power(true)
          case _ => Feedback.Zone2Power(false)
        }
      case str if str.startsWith("ZV")    =>
        Feedback.Zone2Volume(str.diff("ZV").toInt)
      case str if str.startsWith("Z2MUT") =>
        str.diff("Z2MUT").toInt match {
          case 0 => Feedback.Zone2Mute(true)
          case _ => Feedback.Zone2Mute(false)
        }
      case str if str.startsWith("Z2F")   =>
        str.diff("Z2F").toInt match {
          case 4  => Feedback.Zone2Input(DVD)
          case 5  => Feedback.Zone2Input(TV)
          case 15 => Feedback.Zone2Input(DVR)
          case 10 => Feedback.Zone2Input(Video1)
          case 14 => Feedback.Zone2Input(Video2)
          case 26 => Feedback.Zone2Input(InternetRadio)
          case 1  => Feedback.Zone2Input(CD)
          case 2  => Feedback.Zone2Input(CDR)
          case _  => Feedback.Zone2Input(UnknownSource)
        }
      case str if str.startsWith("FL")    =>
        Feedback.Display(str.diff("FL"))
      case str if str.startsWith("RGB")   =>
        Feedback.InputName(str.diff("RGB").drop(2))
      case "R"                            =>
        Feedback.KeepAlive
      case "\r"                           =>
        Feedback.KeepAlive
      case other                          =>
        Feedback.UnknownFeedback(other)
    }
  }
  
}



