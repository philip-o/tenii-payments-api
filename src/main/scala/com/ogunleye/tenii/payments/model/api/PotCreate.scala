package com.ogunleye.tenii.payments.model.api

import com.ogunleye.tenii.payments.model.Pot

case class PotCreateRequest(teniiId: String, limit: Int)

case class PotCreateResponse(pot: Option[Pot], cause: Option[String] = None)
