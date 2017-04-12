//package com.twitter.finagle
//package samantha.feedback
//
//import com.twitter.concurrent.AsyncQueue
//import com.twitter.finagle.samantha.protocol._
//import com.twitter.finagle.util.BufReader
//import com.twitter.io.Buf
//import scala.annotation.tailrec
//import scala.collection.mutable.ListBuffer
//
//private[samantha] final class FeedbackDecoder(prefix: String, suffix: String) {
//
//  private[this] val buffer = new AsyncQueue[Buf]
//
//  private[this] final class Acc(
//    var n: Long,
//    val replies: ListBuffer[Feedback],
//    val finish: List[Feedback] => Feedback)
//
//  private[this] var reader = BufReader(Buf.Empty)
//  private[this] var stack = List.empty[Acc]
//  private[this] var current = init
//
//  def absorb(buf: Buf): Feedback = synchronized {
//    // Absorb the new buffer.
//    reader = BufReader(reader.readAll().concat(buf))
//
//    // Decode the next reply if possible.
//    decodeNext(current)
//  }
//
//  // Tries its best to decode the next _full_ reply or returns `null` if
//  // there is not enough data in the input buffer.
//  @tailrec
//  private[this] def decodeNext(stage: Stage): Feedback = stage(reader) match {
//    case NextStep.Incomplete =>
//      // The decoder is starving so we capture the current state
//      // and fail-fast with `null`.
//      current = stage
//      null
//    case NextStep.Goto(nextStage) => decodeNext(nextStage)
//    case NextStep.Emit(reply) =>
//      stack match {
//        case Nil =>
//          // We finish decoding of a single reply so reset the state.
//          current = init
//          reply
//        case acc :: rest if acc.n == 1 =>
//          stack = rest
//          acc.replies += reply
//          decodeNext(Stage.const(NextStep.Emit(acc.finish(acc.replies.toList))))
//        case acc :: _ =>
//          acc.n -= 1
//          acc.replies += reply
//          decodeNext(init)
//      }
//    case NextStep.Accumulate(n, finish) =>
//      stack = new Acc(n, ListBuffer.empty[Feedback], finish) :: stack
//      decodeNext(init)
//  }
//}