package com.twitter.finagle.samantha.control

sealed trait Code

object Code {
  
  case class IP(name: String, data: String, prefix: Char, suffix: Char) extends Code
  
  case class Hex(name: String, data: String, repeat: Int) extends Code
  
}