package com.ogunleye.tenii.payments.db

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.Imports.MongoDBObject
import com.ogunleye.tenii.payments.model.db.{ TeniiBankAccount, TeniiUser }
import com.typesafe.scalalogging.LazyLogging

class UserConnection extends ObjectMongoConnection[TeniiUser] with LazyLogging {

  val collection = "teniiUser"

  override def transform(obj: TeniiUser): MongoDBObject = {
    MongoDBObject("_id" -> obj.id, "teniiId" -> obj.teniiId, "paymentUserId" -> obj.paymentUserId, "wallet" -> obj.wallet, "accounts" -> obj.accounts, "mandateId" -> obj.mandateId, "destinationAccount" -> obj.destinationAccount)
  }

  def findByUserId(userId: String): Option[TeniiUser] = {
    findByProperty("teniiId", userId, s"No user found with userId: $userId")
  }

  def findById(id: String): Option[TeniiUser] =
    findByObjectId(id, s"No user found with id: $id")

  override def revert(obj: MongoDBObject): TeniiUser = {
    TeniiUser(
      Some(getObjectId(obj, "_id")),
      getString(obj, "teniiId"),
      getString(obj, "paymentUserId"),
      getWallet(obj, "wallet"),
      getOptional[List[TeniiBankAccount]](obj, "accounts"),
      getString(obj, "mandateId"),
      getTeniiBankAccount(obj, "destinationBankAccount")
    )
  }
}
