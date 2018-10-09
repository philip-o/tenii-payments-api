package com.ogunleye.tenii.payments.routes

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{ Directives, Route }
import akka.pattern.{ ask, CircuitBreaker }
import akka.util.Timeout
import com.ogunleye.tenii.payments.actors.PaymentActor
import com.ogunleye.tenii.payments.model.api.{ TeniiTransferRequest, TeniiTransferResponse }
import com.typesafe.scalalogging.LazyLogging
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import javax.ws.rs.Path

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success }

@Path("/pot")
class PotRoute(implicit system: ActorSystem, breaker: CircuitBreaker) extends Directives with LazyLogging {

  implicit val executor: ExecutionContext = system.dispatcher
  implicit val timeout: Timeout = Timeout(10.seconds)
  protected val paymentActor: ActorRef = system.actorOf(Props[PaymentActor])

  def route: Route = pathPrefix("pot") {
    makeTeniiTransfer
  }

  def makeTeniiTransfer: Route =
    post {
      entity(as[TeniiTransferRequest]) { request =>
        logger.info(s"POST /pot - $request")
        onCompleteWithBreaker(breaker)(paymentActor ? request) {
          case Success(msg: TeniiTransferResponse) if msg.cause.isEmpty => complete(StatusCodes.Created -> msg)
          case Success(msg: TeniiTransferResponse) => complete(StatusCodes.InternalServerError -> msg)
          case Failure(t) => failWith(t)
        }
      }
    }

  //  def createTellerPot: Route =
  //  post {
  //
  //  }
}
