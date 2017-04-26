package io.samantha
package protocol

sealed trait Command

sealed trait ReceiverCommand extends Command

object ReceiverCommand {
  
  case object PowerStatus extends ReceiverCommand
  case object PowerOn extends ReceiverCommand
  case object PowerOff extends ReceiverCommand
  
  case object VolumeStatus extends ReceiverCommand
  case object VolumeUp extends ReceiverCommand
  case object VolumeDown extends ReceiverCommand
  case class VolumeSet(level: Int) extends ReceiverCommand
  
  case object MuteStatus extends ReceiverCommand
  case object MuteOn extends ReceiverCommand
  case object MuteOff extends ReceiverCommand
  
  case object InputStatus extends ReceiverCommand
  case object InputCycleUp extends ReceiverCommand
  case object InputCycleDown extends ReceiverCommand
  
  
  
  
  
}