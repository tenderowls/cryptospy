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

/**
  * CoinOne
  * @see http://doc.coinone.co.kr/#api-Public-RecentTransactions
  */
final class CoinOne(scheduler: Scheduler, directory: File) {

  import CoinOne._

  private val loggerName = "Coinone"
  private val logger = LoggerFactory.getLogger(loggerName)
  private val storage = new OrderStorage(directory, "coinone")

  // Main jobs
  private def startJobs(): Unit = {
    logger.info(s"Start to receive orders from CoinOne (${Currencies.mkString(", ")})")
    Currencies foreach { currency =>
      scheduler.schedule(SessionInterval, 0 seconds) {
        val lastTimestamp = storage.lastAppendTimestamp.getOrElse(0L) / 1000L
        val json = Http("https://api.coinone.co.kr/trades")
          .param("currency", currency.toString.toLowerCase)
          .param("format", "json")
          .asString
        val orders = read[Trades](json.body)
          .completeOrders
          .map { completeOrder =>
            val timestamp = completeOrder.timestamp.toLong
            val price = BigDecimal(completeOrder.price)
            val qty = BigDecimal(completeOrder.qty)
            Order(timestamp, Asset(currency, qty), Asset(Currency.KRW, price))
          }
          .filter(_.timestamp > lastTimestamp)
        orders.foreach(order => storage.append(order))
        if (orders.nonEmpty) logger.info(s"Add ${orders.length} $currency/${Currency.KRW} orders")
      }
    }
  }

  startJobs()
}

object CoinOne {

  @pushka case class CompleteOrder(timestamp: String,
                                   price: String,
                                   qty: String)

  @pushka case class Trades(completeOrders: Seq[CompleteOrder], timestamp: String)

  final val SessionInterval = 30 minutes

  final val Currencies = Set(
    Currency.BTC,
    Currency.BCH,
    Currency.ETC,
    Currency.ETH,
    Currency.XRP
  )
}
