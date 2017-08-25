package com.tenderowls.cryptospy.data

case class Order(timestamp: Long, buy: Asset, sell: Asset) {
  def toCsv: String = s"$timestamp,${buy.currency},${buy.qty},${sell.currency},${sell.qty}"
}

object Order {
  def fromCsv(s: String): Order = {
    val Array(timestamp, buyCurrency, buyQty, sellCurrency, sellQty) = s.split(',')
    Order(
      timestamp = timestamp.toLong,
      buy = Asset(
        currency = Currency.fromString(buyCurrency),
        qty = BigDecimal(buyQty)
      ),
      sell = Asset(
        currency = Currency.fromString(sellCurrency),
        qty = BigDecimal(sellQty)
      )
    )
  }
}
