package com.tenderowls.cryptospy.data

sealed abstract class Currency(val label: String)

object Currency {

  // Crypto
  case object BTC extends Currency("Bitcoin")
  case object BCH extends Currency("Bitcoin Cash")
  case object ETH extends Currency("Ethereum")
  case object ETC extends Currency("Ethereum Classic")
  case object XRP extends Currency("Ripple")
  case object XMR extends Currency("Monero")
  case object USDT extends Currency("Tether")
  case object LTC extends Currency("Litecoin")
  case object DASH extends Currency("Dash")
  case object DOGE extends Currency("Doge")
  case object ZEC extends Currency("Zcash")
  case object NXT extends Currency("NXT")
  case object STR extends Currency("Stellar")
  case object REP extends Currency("Augur")
  case object YBC extends Currency("Yuanbao")

  // Fiat
  case object CNY extends Currency("Yuan")
  case object USD extends Currency("US Dollars")
  case object KRW extends Currency("South Korean Won")

  val fromString: (String => Currency) = Map(
    BTC.toString -> BTC,
    BCH.toString -> BCH,
    ETH.toString -> ETH,
    ETC.toString -> ETC,
    XRP.toString -> XRP,
    XMR.toString -> XMR,
    USDT.toString -> USDT,
    LTC.toString -> LTC,
    DASH.toString -> DASH,
    DOGE.toString -> DOGE,
    ZEC.toString -> ZEC,
    NXT.toString -> NXT,
    STR.toString -> STR,
    REP.toString -> REP,
    YBC.toString -> YBC,
    
    USD.toString -> USD,
    KRW.toString -> KRW,
    CNY.toString -> CNY
  )
}
