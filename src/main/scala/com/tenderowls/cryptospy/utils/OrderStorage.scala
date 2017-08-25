package com.tenderowls.cryptospy.utils

import java.io.{File, FileWriter}

import com.tenderowls.cryptospy.data.{Currency, Order}
import pushka.json._
import scala.collection.concurrent.TrieMap

class OrderStorage(logDirectory: File, name: String) {

  private val logFile = new File(logDirectory, name + ".csv")
  private val metadataFile = {
    val file = new File(logDirectory, "metadata")
    if (!file.exists()) file.mkdirs()
    new File(file, name)
  }

  private lazy val logWriter = new FileWriter(logFile, true)
  private val metadata = TrieMap.empty[Currency, Long]

  def append(order: Order): Unit = this.synchronized {
    logWriter.write(order.toCsv + '\n')
    logWriter.flush()
    metadata.put(order.buy.currency, order.timestamp)
    // Write metadata
    val metadataWriter = new FileWriter(metadataFile)
    metadataWriter.write(write(metadata.toMap))
    metadataWriter.flush()
    metadataWriter.close()
  }

  def lastTimestamp(currency: Currency): Long = {
    metadata.getOrElse(currency, 0l)
  }

  if (metadataFile.exists()) {
    val json = io.Source.fromFile(metadataFile).mkString
    read[Map[Currency, Long]](json) foreach {
      case (key, value) => metadata.put(key, value)
    }
  }

  // Close file before shutdown
  Runtime.getRuntime.addShutdownHook {
    new Thread {
      override def run(): Unit = {
        logWriter.close()
      }
    }
  }
}
