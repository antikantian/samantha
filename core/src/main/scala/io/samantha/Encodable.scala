package io.samantha

trait Encodable[A, B] {
  def encode(a: A): B
}