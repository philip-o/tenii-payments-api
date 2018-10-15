package com.ogunleye.tenii.payments.implicits

import com.ogunleye.tenii.payments.model.api.TellerTeniiPotCreateRequest
import com.ogunleye.tenii.payments.model.db.TellerTeniiPot

trait PotImplicit {

  implicit def toTellerTeniiPot(request: TellerTeniiPotCreateRequest): TellerTeniiPot = {
    TellerTeniiPot(
      tellerUserId = request.tellerUserId,
      limit = request.limit,
      amount = 0
    )
  }
}
