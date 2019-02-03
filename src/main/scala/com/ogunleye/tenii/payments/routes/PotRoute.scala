package com.ogunleye.tenii.payments.routes

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.pattern.{CircuitBreaker, ask}
import akka.util.Timeout
import com.ogunleye.tenii.payments.actors.PotActor
import com.ogunleye.tenii.payments.model.api._
import com.typesafe.scalalogging.LazyLogging
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import io.swagger.annotations._
import javax.ws.rs.Path

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

@Path("/pot")
@Api(value = "/pot", description = "User's pot", produces = "application/json")
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

  @Path("{teniiId}/balance")
  @ApiOperation(
    httpMethod = "GET",
    response = classOf[GetPotResponse],
    value = "Get the user's pot",
    consumes = "application/json",
    notes =
      """
         Get the user's pot
      """
  )
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "teniiId", dataType = "string", value = "The tenii Id for the user to find their pot", paramType = "body", required = true)
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Ok", response = classOf[GetPotResponse]),
    new ApiResponse(code = 400, message = "Bad request", response = classOf[ErrorResponse]),
    new ApiResponse(code = 500, message = "Internal Server Error", response = classOf[Throwable])
  ))
  def getPot: Route =
    get {
      path(userIdSegment / "balance").as(GetPotRequest) {
        request =>
          logger.info(s"GET /pot/${request.teniiId}/balance")
          onCompleteWithBreaker(breaker)(potActor ? request) {
            case Success(msg: GetPotResponse) => complete(StatusCodes.OK -> msg)
            case Success(msg: ErrorResponse) => complete(StatusCodes.BadRequest -> msg)
            case Failure(t) => failWith(t)
          }
      }
    }
}
