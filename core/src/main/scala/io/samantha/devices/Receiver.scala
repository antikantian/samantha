package io.samantha.devices

trait Receiver {
  def muted: Boolean
  def volume: String
  def source: String
  def power: Boolean
}