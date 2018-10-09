package com.ogunleye.tenii.payments.actors

import akka.actor.Actor
import com.ogunleye.tenii.payments.implicits.PotImplicit
import com.ogunleye.tenii.payments.db.TellerTeniiPotConnection
import com.ogunleye.tenii.payments.model.api.{ TellerTeniiPotCreditRequest, TellerTeniiPotCreditResponse }
import com.ogunleye.tenii.payments.model.db.TellerTeniiPot
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{ Failure, Success }

class TellerActor extends Actor with LazyLogging with PotImplicit {

  val connection = new TellerTeniiPotConnection

  override def receive: Receive = {
    case request: TellerTeniiPotCreditRequest =>
      val senderRef = sender()
      Future {
        connection.findByUserId(request.tellerUserId)
      } onComplete {
        case Success(potOpt) => potOpt match {
          //TODO Check if amount plus current reaches limit
          case Some(pot) => savePot(pot.copy(amount = pot.amount + request.amount)) onComplete {
            case Success(_) =>
              logger.info(s"Updated the pot for request: $request")
              senderRef ! TellerTeniiPotCreditResponse(request.tellerUserId, None)
            case Failure(t) =>
              logger.error(s"Failed to update pot for request: $request, please check", t)
              senderRef ! TellerTeniiPotCreditResponse(request.tellerUserId, Some(s"Failed to update pot for request: $request, please check"))
          }
          case None => savePot(request) onComplete {
            case Success(_) =>
              logger.info(s"Created the pot for request: $request")
              senderRef ! TellerTeniiPotCreditResponse(request.tellerUserId, None)
            case Failure(t) =>
              logger.error(s"Failed to create pot for request: $request, please check", t)
              senderRef ! TellerTeniiPotCreditResponse(request.tellerUserId, Some(s"Failed to create pot for request: $request, please check"))
          }
        }
        case Failure(t) =>
          logger.error(s"Failed to lookup pot, manually update or create pot then updated.  $request", t)
          senderRef ! TellerTeniiPotCreditResponse(request.tellerUserId, Some(s"Failed to lookup pot, manually update or create pot then updated.  $request"))
      }
  }

  private def savePot(pot: TellerTeniiPot) = Future { connection.save(pot) }
}
