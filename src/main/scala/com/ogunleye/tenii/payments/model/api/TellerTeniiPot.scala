package com.ogunleye.tenii.payments.model.api

case class TellerTeniiPotCreditRequest(tellerUserId: String, amount: Double)

case class TellerTeniiPotCreditResponse(tellerUserId: String, cause: Option[String] = None)

case class TellerTeniiPotCreateRequest(tellerUserId: String, limit: Double)

case class TellerTeniiPotCreateResponse(tellerUserId: String, cause: Option[String] = None)

case class TellerTeniiPotGetRequest(tellerUserId: String)

case class TellerTeniiPotGetResponse(tellerUserId: String, amount: Option[Double] = None, limit: Option[Double] = None, cause: Option[String] = None)