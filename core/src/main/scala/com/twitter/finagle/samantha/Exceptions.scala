package com.twitter.finagle
package samantha

case class ServerError(message: String) extends Exception(message)
case class ClientError(message: String) extends Exception(message)

case class FeedbackCastError(message: String) extends Exception(message)