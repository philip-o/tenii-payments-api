package com.ogunleye.tenii.payments.routes

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{ Directives, Route }
import akka.pattern.{ ask, CircuitBreaker }
import akka.util.Timeout
import com.ogunleye.tenii.payments.actors.PotActor
import com.ogunleye.tenii.payments.model.api.{ TeniiPotCreditRequest, TeniiPotCreditResponse }
import com.typesafe.scalalogging.LazyLogging
import javax.ws.rs.Path
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success }

@Path("/credit")
class PaymentRoute(implicit system: ActorSystem, breaker: CircuitBreaker) extends Directives with LazyLogging {

  implicit val executor: ExecutionContext = system.dispatcher
  implicit val timeout: Timeout = Timeout(10.seconds)
  protected val potActor: ActorRef = system.actorOf(Props[PotActor])

  def route: Route = pathPrefix("credit") {
    creditPot
  }

  def creditPot: Route =
    post {
      entity(as[TeniiPotCreditRequest]) { request =>
        logger.info(s"POST /credit - $request")
        onCompleteWithBreaker(breaker)(potActor ? request) {
          case Success(msg: TeniiPotCreditResponse) if msg.cause.isEmpty => complete(StatusCodes.OK -> msg)
          case Success(msg: TeniiPotCreditResponse) => complete(StatusCodes.InternalServerError -> msg)
          case Failure(t) => failWith(t)
        }
      }
    }
}
