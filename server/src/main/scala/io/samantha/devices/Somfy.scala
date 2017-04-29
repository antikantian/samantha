package io.samantha
package devices

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

import scala.concurrent.duration._

object Somfy extends SomfyProtocol {
  
  //def send(cmd: Action)(implicit A: ActorSystem, M: ActorMaterializer) =
  
}

trait SomfyProtocol {
  
  private val traversalTime = 50.seconds
  private val traverse1Pct = 500.milliseconds
  
  sealed trait SomfyCommand
  
  case object TerraceAllUp extends SomfyCommand
  case object TerraceAllDown extends SomfyCommand
  case object TerraceAllStop extends SomfyCommand
  case object TerraceAllOneQuarter extends SomfyCommand
  case object TerraceAllTwoQuarters extends SomfyCommand
  case object TerraceAllThreeQuarters extends SomfyCommand
  
  sealed trait Action extends SomfyCommand
  
  object Action {
    case object Up extends Action
    case object Down extends Action
    case object Stop extends Action
    
    case object OneQuarterDown
    case object TwoQuartersDown
    case object ThreeQuartersDown
    
  }
}