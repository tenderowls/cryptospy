package com.tenderowls.cryptospy.providers

import java.io.File

import com.tenderowls.cryptospy.data.{Asset, Currency, Order}
import com.tenderowls.cryptospy.utils.{OrderStorage, Scheduler}
import pushka.json._
import pushka.annotation._
import slogging.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps
import scalaj.http._

final class CoinOne(scheduler: Scheduler, directory: File) {

  import CoinOne._

  private val loggerName = "Coinone"
  private val logger = LoggerFactory.getLogger(loggerName)
  private val storage = new OrderStorage(directory, "coinone")

  // Main jobs
  private def startJobs(): Unit = {
    logger.info(s"Start to receive orders from CoinOne (${Currencies.mkString(", ")})")
    Currencies foreach { currency =>
      scheduler.schedule(RequestInterval) {
        val json = Http("https://api.coinone.co.kr/trades")
          .param("currency", currency.toString.toLowerCase)
          .param("format", "json")
          .asString
        val orders = read[Trades](json.body).completeOrders map { completeOrder =>
          val timestamp = completeOrder.timestamp.toLong
          val price = BigDecimal(completeOrder.price)
          val qty = BigDecimal(completeOrder.qty)
          Order(timestamp, Asset(currency, qty), Asset(Currency.KRW, price))
        }
        orders.foreach(order => storage.append(order))
        logger.info(s"Add ${orders.length} $currency/${Currency.KRW} orders")
      }
    }
  }

  private def scheduleFirstRun(): Unit = {
    val now = System.currentTimeMillis()
    val timePassedAfterLastUpdate = storage.lastAppendTimestamp
      .fold(RequestInterval.toMillis)(t => now - t)
      .milliseconds
    if (timePassedAfterLastUpdate >= RequestInterval) {
      startJobs()
    } else {
      val delay = RequestInterval - timePassedAfterLastUpdate
      logger.info(s"Delayed on $delay")
      scheduler.scheduleOnce(delay) {
        startJobs()
      }
    }
  }

  scheduleFirstRun()
}

object CoinOne {

  @pushka case class CompleteOrder(timestamp: String,
                                   price: String,
                                   qty: String)

  @pushka case class Trades(completeOrders: Seq[CompleteOrder], timestamp: String)

  final val RequestInterval = 1 hour

  final val Currencies = Set(
    Currency.BTC,
    Currency.BCH,
    Currency.ETC,
    Currency.ETH,
    Currency.XRP
  )
}
