package com.ogunleye.tenii.payments.implicits

import com.ogunleye.tenii.payments.model.api.TellerTeniiPotCreditRequest
import com.ogunleye.tenii.payments.model.db.TellerTeniiPot

trait PotImplicit {

  implicit def toTellerTeniiPot(request: TellerTeniiPotCreditRequest): TellerTeniiPot = {
    TellerTeniiPot(
      tellerUserId = request.tellerUserId,
      limit = request.limit,
      amount = request.amount
    )
  }
}
