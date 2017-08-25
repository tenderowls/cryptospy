package com.tenderowls.cryptospy.providers

import java.io.File
import java.text.SimpleDateFormat
import java.util.TimeZone

import com.tenderowls.cryptospy.data.{Asset, Currency, Order}
import com.tenderowls.cryptospy.utils.{OrderStorage, Scheduler}
import pushka.json._
import pushka.annotation._
import slogging.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps
import scalaj.http._

final class Poloniex(scheduler: Scheduler, directory: File) {

  import Poloniex._

  private val loggerName = "Poloniex"
  private val logger = LoggerFactory.getLogger(loggerName)
  private val storage = new OrderStorage(directory, "poloniex")

  private def pair(currency: Currency) = s"USDT_$currency"

  private def startJobs(): Unit = {
    logger.info(s"Start to receive orders from Poloniex (${Currencies.mkString(", ")})")
    Currencies foreach { currency =>
      scheduler.schedule(RequestInterval) {
        val now = System.currentTimeMillis().milliseconds
        val start = storage.lastAppendTimestamp
          .map(_.milliseconds)
          .getOrElse(now - InitialOffset)
        val json = Http("https://poloniex.com/public")
          .param("command", "returnTradeHistory")
          .param("currencyPair", pair(currency))
          .param("start", start.toSeconds.toString)
          .param("end", now.toSeconds.toString)
          .asString
        val orders = read[Seq[Entry]](json.body) map { entry =>
          val date = DateFormat.parse(entry.date)
          val timestamp = date.getTime / 1000L
          val buyQty = BigDecimal(entry.total)
          val sellQty = BigDecimal(entry.amount)
          entry.`type` match {
            case "buy" => Order(timestamp, Asset(currency, buyQty), Asset(Currency.USDT, sellQty))
            case "sell" => Order(timestamp, Asset(Currency.USDT, buyQty), Asset(currency, sellQty))
          }
        }
        orders.foreach(order => storage.append(order))
        logger.info(s"Add ${orders.length} $currency/${Currency.USDT} orders")
      }
    }
  }

  startJobs()
}

object Poloniex {

  final val DateFormat = {
    // 2017-08-25 12:43:38
    val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"))
    dateFormat
  }

  // USDT/BTC
  @pushka case class Entry(
    date: String,
    `type`: String,
    globalTradeID: Long,
    total: String,
    amount: String,
    tradeID: Long,
    rate: String,
  )

  final val Currencies = Set(
    Currency.BTC,
    Currency.XRP,
    Currency.ETH,
    Currency.BCH,
    Currency.XMR,
    Currency.LTC,
    Currency.DASH,
    Currency.ZEC,
    Currency.NXT,
    Currency.ETC,
    Currency.STR,
    Currency.REP
  )

  final val InitialOffset = 180 days
  final val RequestInterval = 5 minutes
}
