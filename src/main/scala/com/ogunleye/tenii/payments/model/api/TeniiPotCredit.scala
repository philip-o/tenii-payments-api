package com.ogunleye.tenii.payments.model.api

case class TeniiPotCreditRequest(userId: String, amount: Double)

case class TeniiPotCreditResponse(userId: String, cause: Option[String] = None)