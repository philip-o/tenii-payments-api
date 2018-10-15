package com.ogunleye.tenii.payments.model.api

case class TellerTeniiPotCreditRequest(tellerUserId: String, amount: Double)

case class TellerTeniiPotCreditResponse(tellerUserId: String, cause: Option[String] = None)

case class TellerTeniiPotCreateRequest(tellerUserId: String, limit: Double)

case class TellerTeniiPotCreateResponse(tellerUserId: String, cause: Option[String] = None)
