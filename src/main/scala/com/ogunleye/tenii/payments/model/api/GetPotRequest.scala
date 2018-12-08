package com.ogunleye.tenii.payments.model.api

import com.ogunleye.tenii.payments.model.Pot

case class GetPotRequest(teniiId: String)

case class GetPotResponse(pot: Option[Pot], msg: Option[String] = None)