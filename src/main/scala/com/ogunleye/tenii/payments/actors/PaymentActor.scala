package com.ogunleye.tenii.payments.actors

import akka.actor.Actor
import com.mangopay.MangoPayApi
import com.ogunleye.tenii.payments.config.Settings
import com.ogunleye.tenii.payments.db.UserConnection
import com.ogunleye.tenii.payments.helpers.MangoPayHelper
import com.ogunleye.tenii.payments.model.api.{ TeniiPotCreditRequest, TeniiPotCreditResponse, TeniiTransferRequest, TeniiTransferResponse }
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Failure, Success }

class PaymentActor extends Actor with LazyLogging with MangoPayHelper {

  val api = new MangoPayApi()
  lazy val connection = new UserConnection

  // configuration
  api.Config.ClientId = Settings.clientId
  api.Config.ClientPassword = Settings.clientPassword

  override def receive: Receive = {
    case request: TeniiPotCreditRequest =>
      val senderRef = sender()
      Future {
        connection.findByUserId(request.userId)
      } onComplete {
        case Success(res) => res match {
          case Some(user) =>
            val payout = api.PayIns.create(createDirectDebit(user, request.amount))
            if (payout.Id.toInt > 0) {
              logger.info(s"Credit made successfully $request")
              senderRef ! TeniiPotCreditResponse(request.userId, None)
            } else {
              logger.error(s"Failed to transfer money as per request: $request")
              senderRef ! TeniiPotCreditResponse(request.userId, Some(s"Failed to transfer money as per request: $request"))
            }
          case None =>
            logger.error(s"Failed to find user for request: $request")
            senderRef ! TeniiPotCreditResponse(request.userId, Some(s"Failed to find user for request: $request"))
        }
        case Failure(t) =>
          logger.error(s"Failed to find user for request: $request", t)
          senderRef ! TeniiPotCreditResponse(request.userId, Some(s"Failed to find user for request: $request. ${t.getMessage}"))
      }

    case request: TeniiTransferRequest =>
      val senderRef = sender()
      Future {
        connection.findByUserId(request.userId)
      } onComplete {
        case Success(res) => res match {
          case Some(user) =>
            val transfer = api.PayOuts.create(createTransferOut(user, request.amount))
            if (transfer.Id.toInt > 0) {
              logger.info(s"Pot transfer made successfully $request")
              senderRef ! TeniiTransferResponse(request.userId, None)
            } else {
              logger.error(s"Failed to transfer money as per request: $request")
              senderRef ! TeniiTransferResponse(request.userId, Some(s"Failed to transfer money as per request: $request"))
            }

          case None =>
            logger.error(s"Failed to find user for request: $request")
            senderRef ! TeniiTransferResponse(request.userId, Some(s"Failed to find user for request: $request"))
        }
        case Failure(t) =>
          logger.error(s"Failed to find user for request: $request", t)
          senderRef ! TeniiTransferResponse(request.userId, Some(s"Failed to find user for request: $request. ${t.getMessage}"))
      }

    case other => logger.error(s"Received unknown message $other")
  }

}
