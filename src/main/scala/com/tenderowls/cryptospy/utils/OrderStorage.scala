package com.tenderowls.cryptospy.utils

import java.io.{File, FileWriter}

import com.tenderowls.cryptospy.data.Order

class OrderStorage(logDirectory: File, name: String) {

  private val logFile = new File(logDirectory, name + ".csv")
  private lazy val logWriter = new FileWriter(logFile, true)

  def append(order: Order): Unit = {
    logWriter.write(order.toCsv + '\n')
    logWriter.flush()
  }

  def lastAppendTimestamp: Option[Long] = {
    if (logFile.exists()) Some(logFile.lastModified())
    else None
  }

  // Close file before shutdown
  Runtime.getRuntime.addShutdownHook {
    new Thread {
      override def run() = {
        logWriter.close()

      }
    }
  }
}
