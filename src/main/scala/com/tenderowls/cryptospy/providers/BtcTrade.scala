package com.tenderowls.cryptospy.providers

import java.io.File
import java.net.SocketTimeoutException

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
  * BtcTrade.com
  * @see https://www.btctrade.com/api.help.html?lang=cn
  */
class BtcTrade(scheduler: Scheduler, directory: File) {

  import BtcTrade._

  private val loggerName = "BtcTrade"
  private val logger = LoggerFactory.getLogger(loggerName)
  private val storage = new OrderStorage(directory, "btctrade")

  private def startJobs(): Unit = {
    logger.info(s"Start to receive orders from BtcTrade (${Currencies.mkString(", ")})")
    Currencies.zipWithIndex foreach {
      case (currency, i) =>
        scheduler.schedule(SessionInterval, RequestInterval * i) {
          val lastTimestamp = storage.lastTimestamp(currency)
          val json = try {
            Http("http://api.btctrade.com/api/trades")
              .param("coin", currency.toString.toLowerCase)
              .asString
              .body
          } catch {
            case _: SocketTimeoutException =>
              logger.warn(s"$currency failed fetching because timeout reached")
              "[]"
          }
          val orders = read[Seq[Entry]](json)
            .map { entry =>
              val timestamp = entry.date.toLong
              Order(timestamp, Asset(currency, entry.amount), Asset(Currency.CNY, entry.price))
            }
            .filter(_.timestamp > lastTimestamp)
            .sortBy(_.timestamp)
          orders.foreach(order => storage.append(order))
          if (orders.nonEmpty) logger.info(s"Add ${orders.length} $currency/${Currency.CNY} orders")
        }
    }
  }

  startJobs()
}

object BtcTrade {

  import com.tenderowls.cryptospy.utils.PushkaRws._

  @pushka case class Entry(
    tid: String,
    price: BigDecimal,
    amount: BigDecimal,
    date : String,
    `type`: String
  )

  final val Currencies = Set(
    Currency.BTC,
    Currency.ETH,
    Currency.ETC,
    Currency.LTC,
    Currency.DOGE,
    Currency.YBC
  )

  final val RequestInterval = 1 minute
  final val SessionInterval = Currencies.size * RequestInterval
}
