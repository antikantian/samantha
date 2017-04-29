package io.samantha.protocol

import java.util.UUID

trait Message[A] {
  def id: UUID
  def timestamp: Long
  def payload: A
}

trait Incoming extends Message[Incoming]

trait Outgoing extends Message[Outgoing]

