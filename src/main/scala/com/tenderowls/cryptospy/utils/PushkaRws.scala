package com.tenderowls.cryptospy.utils

import pushka.{Ast, PushkaException, Reader, Writer}

object PushkaRws {
  implicit val bigDecimalReader: Reader[BigDecimal] = {
    case Ast.Num(num) => BigDecimal(num)
    case value => throw PushkaException(value, classOf[BigDecimal])
  }

  implicit val bigDecimalWriter: Writer[BigDecimal] = { value =>
    Ast.Num(value.toString())
  }
}
