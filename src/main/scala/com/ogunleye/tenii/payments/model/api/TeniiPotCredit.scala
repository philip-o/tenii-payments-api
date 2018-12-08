package com.ogunleye.tenii.payments.model.api

import com.ogunleye.tenii.payments.model.Pot

case class TeniiPotCreditRequest(teniiId: String, amount: Double)

case class TeniiPotCreditResponse(pot: Option[Pot], cause: Option[String] = None)