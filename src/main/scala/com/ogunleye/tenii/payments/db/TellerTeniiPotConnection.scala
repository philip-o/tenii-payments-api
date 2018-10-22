package com.ogunleye.tenii.payments.db

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.Imports.MongoDBObject
import com.ogunleye.tenii.payments.model.db.TellerTeniiPot
import com.typesafe.scalalogging.LazyLogging

class TellerTeniiPotConnection extends ObjectMongoConnection[TellerTeniiPot] with LazyLogging {

  val collection = "tellerTeniiPotConnection"

  override def transform(obj: TellerTeniiPot): MongoDBObject = {
    MongoDBObject("_id" -> obj.id, "tellerUserId" -> obj.tellerUserId, "limit" -> obj.limit, "amount" -> obj.amount)
  }

  def findByUserId(userId: String): Option[TellerTeniiPot] = {
    findByProperty("tellerUserId", userId, s"No user found with tellerUserId: $userId")
  }

  override def revert(obj: MongoDBObject): TellerTeniiPot = {
    TellerTeniiPot(
      Some(getObjectId(obj, "_id")),
      getString(obj, "tellerUserId"),
      getDouble(obj, "limit").toInt,
      getDouble(obj, "amount")
    )
  }
}
