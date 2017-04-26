package io.samantha.library
package receivers

case class PioneerVSX[F[_], I, O](addr: String, port: Int) extends Receiver[F, I, O] {
  
  def suffix = "\r"
  
  def powerOn = "PO"
  
  def powerOff = "PF"
  
  def powerStatus = "?P"
  
  def volumeUp = "VU"
  
  def volumeDown = "VD"
  
  def volume(vol: Int) = s"${vol}VL"
  
  def volumeStatus = "?V"
  
  def muteOn = "MO"
  
  def muteOff = "MF"
  
  def muteStatus = "?M"
  
  def input(i: Int) = s"${i}FN"
  
  def inputCycleUp = "FU"
  
  def inputCycleDown = "FD"
  
  def inputStatus = "?F"
  
  def listeningMode(mode: Int) = s"${mode}SR"
  
  def listeningModeStatus = "?S"
  
  def zone2PowerOn = "APO"
  
  def zone2PowerOff = "APF"
  
  def zone2PowerStatus = "?AP"
  
  def zone2Input(i: Int) = s"${i}ZS"
  
  def zone2InputStatus = "?ZS"
  
  def zone2VolumeUp = "ZU"
  
  def zone2VolumeDown = "ZD"
  
  def zone2Volume(vol: Int) = s"${vol}ZV"
  
  def zone2MuteOn = "Z2MO"
  
  def zone2MuteOff = "Z2MF"
  
  def zone2MuteStatus = "?Z2M"
  
  def displayInfo = "?FL"
  
  def audioInfo = "?AST"
  
  def videoInfo = "?VST"
  
  def inputNameInfo(input: Int) = s"?RGB$input"
  
}