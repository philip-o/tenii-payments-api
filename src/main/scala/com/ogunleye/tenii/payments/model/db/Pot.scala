package com.ogunleye.tenii.payments.model.db

import org.bson.types.ObjectId

case class Pot(id: Option[ObjectId] = None, teniiId: String, limit: Int, amount: Double)