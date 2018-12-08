package com.ogunleye.tenii.payments.implicits

import com.ogunleye.tenii.payments.model.api.PotCreateRequest
import com.ogunleye.tenii.payments.model.db.Pot
import com.ogunleye.tenii.payments.model.{ Pot => APIPot}

trait PotImplicit {

  implicit def toPot(pot: PotCreateRequest) = {
    Pot(
      teniiId = pot.teniiId,
      limit = pot.limit,
      amount = 0
    )
  }

  implicit def toAPIPot(pot: Pot) = {
    APIPot(
      pot.teniiId,
      pot.limit,
      pot.amount
    )
  }
}
