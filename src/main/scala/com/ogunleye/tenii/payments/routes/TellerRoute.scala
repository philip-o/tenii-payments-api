package com.ogunleye.tenii.payments.routes

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import akka.pattern.{CircuitBreaker, ask}
import akka.util.Timeout
import com.ogunleye.tenii.payments.actors.TellerActor
import com.ogunleye.tenii.payments.model.api._
import com.typesafe.scalalogging.LazyLogging
import javax.ws.rs.Path
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

@Path("/teller")
class TellerRoute(implicit system: ActorSystem, breaker: CircuitBreaker) extends RequestDirectives with LazyLogging {

  implicit val executor: ExecutionContext = system.dispatcher
  implicit val timeout: Timeout = Timeout(10.seconds)
  protected val tellerActor: ActorRef = system.actorOf(Props[TellerActor])

  def route: Route = pathPrefix("teller") {
    addToTeniiPot ~ createPot ~ getPot
  }

  def addToTeniiPot: Route =
    post {
      entity(as[TellerTeniiPotCreditRequest]) { request =>
        logger.info(s"POST /teller - $request")
        onCompleteWithBreaker(breaker)(tellerActor ? request) {
          case Success(msg: TellerTeniiPotCreditResponse) if msg.cause.isEmpty => complete(StatusCodes.OK -> msg)
          case Success(msg: TellerTeniiPotCreditResponse) => complete(StatusCodes.InternalServerError -> msg)
          case Failure(t) => failWith(t)
        }
      }
    }

  def createPot: Route =
    post {
      (path("createPot") & entity(as[TellerTeniiPotCreateRequest])) { request =>
        logger.info(s"POST /teller/createPot - $request")
        onCompleteWithBreaker(breaker)(tellerActor ? request) {
          case Success(msg: TellerTeniiPotCreateResponse) if msg.cause.isEmpty => complete(StatusCodes.Created -> msg)
          case Success(msg: TellerTeniiPotCreateResponse) => complete(StatusCodes.InternalServerError -> msg)
          case Failure(t) => failWith(t)
        }
      }
    }

  def getPot: Route =
    get {
      path(potSegment).as(TellerTeniiPotGetRequest) { request =>
        logger.info(s"POST /teller/${request.tellerUserId}")
        onCompleteWithBreaker(breaker)(tellerActor ? request) {
          case Success(msg: TellerTeniiPotGetResponse) if msg.cause.isEmpty => complete(StatusCodes.OK -> msg)
          case Success(msg: TellerTeniiPotGetResponse) => complete(StatusCodes.InternalServerError -> msg)
          case Failure(t) => failWith(t)
        }
      }
    }
}
