package com.ogunleye.tenii.payments.actors

import akka.actor.Actor
import com.ogunleye.tenii.payments.db.PotConnection
import com.ogunleye.tenii.payments.implicits.PotImplicit
import com.ogunleye.tenii.payments.model.Pot
import com.ogunleye.tenii.payments.model.api._
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class PotActor extends Actor with LazyLogging with PotImplicit {

  lazy val connection = new PotConnection

  override def receive: Receive = {
    case request: TeniiPotCreditRequest =>
      val senderRef = sender()
      Future {
        connection.findByUserId(request.teniiId)
      } onComplete {
        case Success(res) => res match {
          case Some(pot) => val p = pot.copy(amount = roundTo2DPAsDouble(pot.amount + request.amount))
            logger.info(s"Pot value before: ${pot.amount}, now ${p.amount}")
            Future {
              connection.save(p)
            } onComplete {
              case Success(_) => senderRef ! TeniiPotCreditResponse(Some(p))
              case Failure(t) => logger.error(s"Failed to update pot as per request: $request", t)
                senderRef ! TeniiPotCreditResponse(None, Some(s"Failed to update pot"))
            }
          case None => logger.error(s"Failed to find pot for request: $request")
            senderRef ! TeniiPotCreditResponse(None, Some(s"Failed to find pot for request: $request"))
        }
        case Failure(t) => logger.error(s"Failed to find pot for request: $request", t)
          senderRef ! TeniiPotCreditResponse(None, Some(s"Failed to find pot for request: $request"))
      }

    case request: GetPotRequest =>
      val senderRef = sender()
      Future {
        connection.findByUserId(request.teniiId)
      } onComplete {
        case Success(res) => res match {
          case Some(pot) => senderRef ! GetPotResponse(pot)
          case None => logger.error(s"Failed to find pot for request: ${request.teniiId}")
            senderRef ! ErrorResponse("NO_USER", Some(s"Failed to find pot for request: ${request.teniiId}"))
        }
        case Failure(t) => logger.error(s"Failed to find pot for request: $request", t)
          senderRef ! ErrorResponse("SEARCH_FAILURE", Some(s"Search failed due to ${t.getMessage}"))
      }
//
//    case request: TeniiTransferRequest =>
//      val senderRef = sender()
//      Future {
//        connection.findByUserId(request.userId)
//      } onComplete {
//        case Success(res) => res match {
//          case Some(user) =>
//            val transfer = api.PayOuts.create(createTransferOut(user, request.amount))
//            if (transfer.Id.toInt > 0) {
//              logger.info(s"Pot transfer made successfully $request")
//              senderRef ! TeniiTransferResponse(request.userId, None)
//            } else {
//              logger.error(s"Failed to transfer money as per request: $request")
//              senderRef ! TeniiTransferResponse(request.userId, Some(s"Failed to transfer money as per request: $request"))
//            }
//
//          case None =>
//            logger.error(s"Failed to find user for request: $request")
//            senderRef ! TeniiTransferResponse(request.userId, Some(s"Failed to find user for request: $request"))
//        }
//        case Failure(t) =>
//          logger.error(s"Failed to find user for request: $request", t)
//          senderRef ! TeniiTransferResponse(request.userId, Some(s"Failed to find user for request: $request. ${t.getMessage}"))
//      }

    case request: PotCreateRequest =>
      val senderRef = sender()
      Future {
        connection.save(request)
      } onComplete {
        case Success(_) => logger.info(s"Pot created for user: ${request.teniiId}")
          senderRef ! PotCreateResponse(Some(Pot(request.teniiId, request.limit)))
        case Failure(t) => logger.error(s"Failed to create pot for request: $request", t)
          senderRef ! PotCreateResponse(None, Some("Failed to create pot"))
      }

    case other => logger.error(s"Received unknown message $other")
  }

  def roundTo2DPAsDouble(value: Double): Double = {
    BigDecimal(value).setScale(2, BigDecimal.RoundingMode.UP).toDouble
  }

}
