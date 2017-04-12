package com.twitter.finagle
package samantha.network

import com.twitter.finagle.Stack

object NetworkConfig {
  
  case class CommandPrefix(cp: Option[String])
  implicit object CommandPrefix extends Stack.Param[CommandPrefix] {
    val default = CommandPrefix(None)
  }
  
  case class CommandSuffix(cs: Option[String])
  implicit object CommandSuffix extends Stack.Param[CommandSuffix] {
    val default = CommandSuffix(None)
  }
  
  def apply(prms: Stack.Params): NetworkConfig = {
    val CommandPrefix(p) = prms[CommandPrefix]
    val CommandSuffix(s) = prms[CommandSuffix]
    NetworkConfig(
      commandPrefix = p,
      commandSuffix = s
    )
  }
  
}

case class NetworkConfig(
  commandPrefix: Option[String] = None,
  commandSuffix: Option[String] = None
)