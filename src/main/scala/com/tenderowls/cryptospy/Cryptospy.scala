package com.tenderowls.cryptospy

import java.io.File

import com.tenderowls.cryptospy.providers.{BtcTrade, CoinOne, Poloniex}
import com.tenderowls.cryptospy.utils.Scheduler
import slogging.{LoggerConfig, PrintLogger, PrintLoggerFactory}

object Cryptospy extends App {

  PrintLogger.printTimestamp = true
  LoggerConfig.factory = PrintLoggerFactory()

  val scheduler = new Scheduler()
  val directory = {
    val maybePath = sys.env.get("CRYPTOSPY_DIRECTORY").orElse {
      if (args.length > 0) Some(args(0))
      else None
    }
    maybePath match {
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
        PrintLogger.error("Cryptospy", "CRYPTOSPY_DIRECTORY environment variable or first arg should be defined")
        sys.exit(1)
    }
  }

  val btcTrade = new BtcTrade(scheduler, directory)
  val coinone = new CoinOne(scheduler, directory)
  val poloniex = new Poloniex(scheduler, directory)

  // Wait
  Thread.currentThread.join()
}
