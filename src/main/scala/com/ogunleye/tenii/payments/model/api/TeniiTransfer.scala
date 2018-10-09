package com.ogunleye.tenii.payments.model.api

case class TeniiTransferRequest(userId: String, amount: Double)

case class TeniiTransferResponse(userId: String, cause: Option[String] = None)
