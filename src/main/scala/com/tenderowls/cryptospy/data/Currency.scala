package com.tenderowls.cryptospy.data

sealed abstract class Currency(val label: String)

object Currency {

  // Crypto
  case object BTC extends Currency("Bitcoin")
  case object BCH extends Currency("Bitcoin Cash")
  case object ETH extends Currency("Ethereum")
  case object ETC extends Currency("Ethereum Classic")
  case object XRP extends Currency("Ripple")
  
  // Fiat
  case object USD extends Currency("US Dollars")
  case object KRW extends Currency("South Korean Won")
  
  val fromString: (String => Currency) = Map(
    BTC.toString -> BTC,
    BCH.toString -> BCH,
    ETH.toString -> ETH,
    ETC.toString -> ETC,
    XRP.toString -> XRP,

    USD.toString -> USD,
    KRW.toString -> KRW
  )
}
