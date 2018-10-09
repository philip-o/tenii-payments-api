package com.ogunleye.tenii.payments.actors

import akka.actor.Actor
import com.mangopay.MangoPayApi
import com.ogunleye.tenii.payments.config.Settings
import com.ogunleye.tenii.payments.db.UserConnection
import com.ogunleye.tenii.payments.helpers.MangoPayHelper
import com.ogunleye.tenii.payments.model.api.{ CreateTeniiPaymentUserRequest, CreateTeniiPaymentUserResponse }
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Failure, Success }

class UserActor extends Actor with MangoPayHelper with LazyLogging {

  val api = new MangoPayApi()
  lazy val connection = new UserConnection

  // configuration
  api.Config.ClientId = Settings.clientId
  api.Config.ClientPassword = Settings.clientPassword

  override def receive: Receive = {
    case request: CreateTeniiPaymentUserRequest =>

      val senderRef = sender()

      //TODO Should check user doesn't exist, if they exist raise an error
      val userResult = api.Users.create(request)
      if (userResult.Id.toInt > 0) {
        val wallet = createWallet(request, userResult)

        val walletResult = api.Wallets.create(wallet)
        if (walletResult.Id.toInt > 0) {
          val sourceBankAccount = createBankAccount(request, userResult)
          val destinationBankAccount = createBankAccount(request, userResult)
          val sourceAccountResult = api.Users.createBankAccount(userResult.Id, sourceBankAccount)
          val destinationAccountResult = api.Users.createBankAccount(userResult.Id, destinationBankAccount)
          if (sourceAccountResult.Id.toInt > 0 && destinationAccountResult.Id.toInt > 0) {
            val mandate = api.Mandates.create(createMandate(request, sourceAccountResult))
            if (mandate.Id.toInt > 0) {
              logger.info(s"Mandate, account, wallet and bank accounts created successfully for user ${request.firstName} ${request.surname} in MangoPay")
              val user = createTeniiUser(request, userResult, walletResult, sourceAccountResult, mandate, destinationAccountResult)
              Future {
                connection.save(user)
              } onComplete {
                case Success(_) => senderRef ! CreateTeniiPaymentUserResponse(request.teniiUserId)
                case Failure(t) =>
                  senderRef ! CreateTeniiPaymentUserResponse(request.teniiUserId, Some(s"Failed to persist user due to: ${t.getMessage}"))
                  logger.error(s"Failed to persist user: $user", t)
              }
            } else {
              senderRef ! CreateTeniiPaymentUserResponse(request.teniiUserId, Some("Failed to create mandate"))
              logger.error(s"Failed to create mandate: $mandate")
            }
          } else {
            senderRef ! CreateTeniiPaymentUserResponse(request.teniiUserId, Some("Failed to create bank account"))
            logger.error(s"Failed to create source bank account: $sourceBankAccount or destination bank account: $destinationBankAccount")
          }
        } else {
          senderRef ! CreateTeniiPaymentUserResponse(request.teniiUserId, Some("Failed to create wallet"))
          logger.error(s"Failed to create wallet: $wallet")
        }
      } else {
        senderRef ! CreateTeniiPaymentUserResponse(request.teniiUserId, Some("Failed to create user"))
        logger.error(s"Failed to create user: $userResult")
      }
  }
}
