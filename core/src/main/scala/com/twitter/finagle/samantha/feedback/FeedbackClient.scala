//package com.twitter.finagle
//package samantha.feedback
//
//import com.twitter.conversions.time._
//import com.twitter.finagle.{ Service, ServiceClosedException }
//import com.twitter.finagle.samantha.Client
//import com.twitter.finagle.samantha.protocol._
//import com.twitter.finagle.samantha.util.BufToString
//import com.twitter.io.Buf
//import com.twitter.logging.Logger
//import com.twitter.util.{Future, Futures, Throw, Timer}
//import java.util.concurrent.ConcurrentHashMap
//import scala.collection.JavaConverters._
//import scala.util.control.NonFatal
//
//sealed trait FeedbackHandler {
//  def onSuccess(channel: Buf, node: Service[Command, Feedback]): Unit
//  def onException(node: Service[Command, Feedback], ex: Throwable): Unit
//  def onFeedback(fb: Feedback): Unit
//}
//
//sealed trait