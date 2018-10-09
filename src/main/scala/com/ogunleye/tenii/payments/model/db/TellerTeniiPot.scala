package com.ogunleye.tenii.payments.model.db

import org.bson.types.ObjectId

case class TellerTeniiPot(id: Option[ObjectId] = None, tellerUserId: String, limit: Double, amount: Double)
