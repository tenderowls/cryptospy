package com.tenderowls.cryptospy

import java.io.File

import com.tenderowls.cryptospy.providers.{BtcTrade, CoinOne, Poloniex}
import com.tenderowls.cryptospy.utils.Scheduler
import slogging.{LoggerConfig, PrintLogger, PrintLoggerFactory}

object Cryptospy extends App {

  PrintLogger.printTimestamp = true
  LoggerConfig.factory = PrintLoggerFactory()

  val scheduler = new Scheduler()
  val directory = sys.env.get("CRYPTOSPY_DIRECTORY") match {
    case Some(path) =>
      val file = new File(path)
      if (!file.exists()) file.mkdirs()
      if (!file.isDirectory) {
        PrintLogger.error("Cryptospy", "CRYPTOSPY_DIRECTORY is not a directory")
        sys.exit(1)
      } else {
        file
      }
    case None =>
      PrintLogger.error("Cryptospy", "CRYPTOSPY_DIRECTORY environment variable should be defined")
      sys.exit(1)
  }

  val btcTrade = new BtcTrade(scheduler, directory)
  val coinone = new CoinOne(scheduler, directory)
  val poloniex = new Poloniex(scheduler, directory)

  // Wait
  Thread.currentThread.join()
}
