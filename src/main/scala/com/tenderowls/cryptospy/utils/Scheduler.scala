package com.tenderowls.cryptospy.utils

import java.util.{Timer, TimerTask}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.Success

class Scheduler {

  import Scheduler._

  private val timer = new Timer()

  def scheduleOnce[T](delay: FiniteDuration)(job: => T)(implicit ec: ExecutionContext): JobHandler[T] = {
    val promise = Promise[T]
    val task = new TimerTask {
      def run(): Unit = {
        ec.execute { () =>
          val result = job // Execute a job
          promise.complete(Success(result))
        }
      }
    }
    timer.schedule(task, delay.toMillis)
    JobHandler(
      cancel = () => { task.cancel(); () },
      result = promise.future
    )
  }

  def schedule[U](interval: FiniteDuration, delay: FiniteDuration)(job: => U)(implicit ec: ExecutionContext): Cancel = {
    val task = new TimerTask {
      def run(): Unit = ec.execute(() => job)
    }
    timer.schedule(task, delay.toMillis, interval.toMillis)
    () => { task.cancel(); () }
  }
}

object Scheduler {

  def apply(): Scheduler = new Scheduler()

  type Cancel = () => Unit

  case class JobHandler[+T](cancel: Cancel, result: Future[T])
}

