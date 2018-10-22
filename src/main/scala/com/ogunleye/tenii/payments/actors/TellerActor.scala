package com.ogunleye.tenii.payments.actors

import akka.actor.Actor
import com.ogunleye.tenii.payments.implicits.PotImplicit
import com.ogunleye.tenii.payments.db.TellerTeniiPotConnection
import com.ogunleye.tenii.payments.model.api._
import com.ogunleye.tenii.payments.model.db.TellerTeniiPot
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

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
          case None => senderRef ! TellerTeniiPotCreditResponse(request.tellerUserId, Some(s"Failed to find pot for request: $request, please check"))
        }
        case Failure(t) =>
          logger.error(s"Failed to lookup pot, manually update or create pot then updated.  $request", t)
          senderRef ! TellerTeniiPotCreditResponse(request.tellerUserId, Some(s"Failed to lookup pot, manually update or create pot then updated.  $request"))
      }

    case req: TellerTeniiPotCreateRequest =>
      val senderRef = sender()
      Future {
        connection.findByUserId(req.tellerUserId)
      } onComplete {
        case Success(pot) if pot.isDefined =>
          val resp = s"Pot already exists for user, please check why a new one is being created; $req"
          logger.error(resp)
          senderRef ! TellerTeniiPotCreateResponse(req.tellerUserId, Some(resp))
        case Success(pot) if pot.isEmpty => Future {
          connection.save(req)
        } onComplete {
          case Success(_) => logger.info(s"Created new pot for $req")
            senderRef ! TellerTeniiPotCreateResponse(req.tellerUserId)
          case Failure(t) => val err = s"Failure when trying to save pot"
            logger.error(err, t)
            senderRef ! TellerTeniiPotCreateResponse(req.tellerUserId, Some(err))
        }
      }

    case request: TellerTeniiPotGetRequest =>
      val senderRef = sender()
      Future {
        connection.findByUserId(request.tellerUserId)
      } onComplete {
        case Success(potOpt) => potOpt match {
          case Some(pot) => senderRef ! TellerTeniiPotGetResponse(request.tellerUserId, Some(pot.amount), Some(pot.limit))
          case None => senderRef ! TellerTeniiPotGetResponse(request.tellerUserId, cause = Some(s"Failed to find pot for request: $request, please check"))
        }
        case Failure(t) =>
          logger.error(s"Failed to find pot.  $request", t)
          senderRef ! TellerTeniiPotGetResponse(request.tellerUserId, cause = Some(s"Failed to find pot.  $request"))
      }

    case other => logger.error(s"Received unknown message $other")
  }

  private def savePot(pot: TellerTeniiPot) = Future { connection.save(pot) }
}
