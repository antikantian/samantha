package io.samantha

trait Decodable[A, B] {
  def decode(a: A): B
}