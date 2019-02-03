package com.ogunleye.tenii.payments.model.api

case class GetPotRequest(teniiId: String)

case class PotResponse(limit: Int, amount: Double)

case class GetPotResponse(pot: PotResponse)

case class ErrorResponse(code: String, msg: Option[String] = None)