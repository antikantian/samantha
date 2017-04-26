package com.twitter.finagle
package samantha

import com.twitter.finagle.transport.Transport
import com.twitter.io.Buf

object Config {
  
  case class CommandPrefix(cp: Option[String])
  implicit object CommandPrefix extends Stack.Param[CommandPrefix] {
    val default = CommandPrefix(None)
  }
  
  case class CommandSuffix(cs: Option[String])
  implicit object CommandSuffix extends Stack.Param[CommandSuffix] {
    val default = CommandSuffix(None)
  }
  
  def apply(prms: Stack.Params): Config = {
    val CommandPrefix(p) = prms[CommandPrefix]
    val CommandSuffix(s) = prms[CommandSuffix]
    Config(
      commandPrefix = p,
      commandSuffix = s
    )
  }
  
}

case class Config(
  commandPrefix: Option[String] = None,
  commandSuffix: Option[String] = None
)