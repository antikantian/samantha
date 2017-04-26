package io.samantha
package control

import com.twitter.finagle.Service
import com.twitter.finagle.Samantha
import com.twitter.finagle.samantha.protocol.{ Command, Feedback }

trait Device {
  
  def address: String
  
  def port: Int
  
  def connection: Service[Command, Feedback]
  
}