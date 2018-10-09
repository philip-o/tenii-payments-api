package com.ogunleye.tenii.payments.routes

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{ Directives, Route }
import akka.pattern.{ ask, CircuitBreaker }
import akka.util.Timeout
import com.ogunleye.tenii.payments.actors.UserActor
import com.ogunleye.tenii.payments.model.api.{ CreateTeniiPaymentUserRequest, CreateTeniiPaymentUserResponse }
import com.typesafe.scalalogging.LazyLogging
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import javax.ws.rs.Path

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{ Failure, Success }

@Path("/user")
class UserRoute(implicit system: ActorSystem, breaker: CircuitBreaker) extends Directives with LazyLogging {

  implicit val executor: ExecutionContext = system.dispatcher
  implicit val timeout: Timeout = Timeout(10.seconds)
  protected val userActor: ActorRef = system.actorOf(Props[UserActor])

  def route: Route = pathPrefix("user") {
    addUser()
  }

  def addUser(): Route =
    post {
      entity(as[CreateTeniiPaymentUserRequest]) { request =>
        logger.info(s"POST /user - $request")
        onCompleteWithBreaker(breaker)(userActor ? request) {
          case Success(msg: CreateTeniiPaymentUserResponse) if msg.cause.isEmpty => complete(StatusCodes.Created -> msg)
          case Success(msg: CreateTeniiPaymentUserResponse) => complete(StatusCodes.InternalServerError -> msg)
          case Failure(t) => failWith(t)
        }
      }
    }
}
