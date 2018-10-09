package com.ogunleye.tenii.payments.model.db

import org.bson.types.ObjectId

case class TeniiUser(id: Option[ObjectId] = None, teniiId: String, paymentUserId: String, wallet: TeniiPot, accounts: Option[List[TeniiBankAccount]] = None, mandateId: String, destinationAccount: TeniiBankAccount)

case class TeniiBankAccount(id: String, accountNumber: String, sortCode: String, provider: String)

case class TeniiPot(id: String, amount: Double = 0)