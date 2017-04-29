package com.twitter.finagle.samantha

import com.twitter.io.Buf

trait Message

trait Incoming extends Message {
  def decode[A](buf: Buf): A
}

trait Outgoing[A] extends Message {
  def encode(msg: A): Buf
}