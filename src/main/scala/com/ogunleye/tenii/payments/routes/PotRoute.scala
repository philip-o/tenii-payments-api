package com.ogunleye.tenii.payments.routes

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import akka.pattern.{CircuitBreaker, ask}
import akka.util.Timeout
import com.ogunleye.tenii.payments.actors.PotActor
import com.ogunleye.tenii.payments.model.api._
import com.typesafe.scalalogging.LazyLogging
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import javax.ws.rs.Path

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

@Path("/pot")
class PotRoute(implicit system: ActorSystem, breaker: CircuitBreaker) extends RequestDirectives with LazyLogging {

  implicit val executor: ExecutionContext = system.dispatcher
  implicit val timeout: Timeout = Timeout(10.seconds)
  protected val potActor: ActorRef = system.actorOf(Props[PotActor])

  def route: Route = pathPrefix("pot") {
    makeTeniiTransfer ~ createTeniiPot ~ getPot
  }

  def makeTeniiTransfer: Route =
    post {
      path("transfer") {
      entity(as[TeniiTransferRequest]) { request =>
        logger.info(s"POST /pot/transfer - $request")
        onCompleteWithBreaker(breaker)(potActor ? request) {
          case Success(msg: TeniiTransferResponse) if msg.cause.isEmpty => complete(StatusCodes.OK -> msg)
          case Success(msg: TeniiTransferResponse) => complete(StatusCodes.InternalServerError -> msg)
          case Failure(t) => failWith(t)
        }
      }
    }
    }

    def createTeniiPot: Route =
      post {
          entity(as[PotCreateRequest]) { request =>
            logger.info(s"POST /pot - $request")
            onCompleteWithBreaker(breaker)(potActor ? request) {
              case Success(msg: PotCreateResponse) if msg.cause.isEmpty => complete(StatusCodes.Created -> msg)
              case Success(msg: PotCreateResponse) => complete(StatusCodes.InternalServerError -> msg)
              case Failure(t) => failWith(t)
            }
          }
      }

  def getPot: Route =
    get {
      path(userIdSegment / "balance").as(GetPotRequest) {
        request =>
          logger.info(s"GET /pot/${request.teniiId}/balance")
          onCompleteWithBreaker(breaker)(potActor ? request) {
            case Success(msg: GetPotResponse) if msg.msg.isEmpty => complete(StatusCodes.OK -> msg)
            case Success(msg: GetPotResponse) => complete(StatusCodes.InternalServerError -> msg)
            case Failure(t) => failWith(t)
          }
      }
    }
}
