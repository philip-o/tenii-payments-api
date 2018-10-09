package com.ogunleye.tenii.payments.model.api

case class TellerTeniiPotCreditRequest(tellerUserId: String, limit: Double, amount: Double)

case class TellerTeniiPotCreditResponse(tellerUserId: String, cause: Option[String] = None)
