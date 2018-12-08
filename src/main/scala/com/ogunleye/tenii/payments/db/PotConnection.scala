package com.ogunleye.tenii.payments.db

import com.mongodb.casbah.Imports.{MongoDBObject, _}
import com.ogunleye.tenii.payments.model.db.Pot
import com.typesafe.scalalogging.LazyLogging

class PotConnection extends ObjectMongoConnection[Pot] with LazyLogging {

  val collection = "pot"

  override def transform(obj: Pot): MongoDBObject = {
    MongoDBObject("_id" -> obj.id, "teniiId" -> obj.teniiId, "limit" -> obj.limit, "amount" -> obj.amount)
  }

  def findByUserId(teniiId: String): Option[Pot] = {
    findByProperty("teniiId", teniiId, s"No pot found with teniiId: $teniiId")
  }

  override def revert(obj: MongoDBObject): Pot = {
    Pot(
      Some(getObjectId(obj, "_id")),
      getString(obj, "teniiId"),
      getInt(obj, "limit"),
      getDouble(obj, "amount")
    )
  }
}
