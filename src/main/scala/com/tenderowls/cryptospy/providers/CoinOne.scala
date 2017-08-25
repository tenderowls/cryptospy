package com.tenderowls.cryptospy.providers

import java.io.File

import com.tenderowls.cryptospy.data.{Asset, Currency, Order}
import com.tenderowls.cryptospy.utils.{OrderStorage, Scheduler}
import pushka.annotation._
import pushka.json._
import slogging.LazyLogging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps
import scalaj.http._

class CoinOne(scheduler: Scheduler, directory: File) extends LazyLogging {

  import CoinOne._

  val storage = new OrderStorage(directory, "coinone")

  // Main jobs
  private def startJobs(): Unit = {
    logger.info("Started receiving orders from CoinOne")
    logger.info(s"Currencies: ${Currencies.mkString(", ")}")
    Currencies foreach { currency =>
      scheduler.schedule(RequestInterval) {
        val json = Http("https://api.coinone.co.kr/trades")
          .param("currency", currency.toString.toLowerCase)
          .param("format", "json")
          .asString
        logger.info(s"Orders for $currency/${Currency.KRW} received")
        val orders = read[Trades](json.body).completeOrders map { case completeOrder =>
          val timestamp = completeOrder.timestamp.toLong
          val price = BigDecimal(completeOrder.price)
          val qty = BigDecimal(completeOrder.qty)
          Order(timestamp, Asset(currency, qty), Asset(Currency.KRW, price))
        }
        orders.foreach(order => storage.append(order))
        logger.info(s"${orders.length} $currency/${Currency.KRW} orders added")
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
