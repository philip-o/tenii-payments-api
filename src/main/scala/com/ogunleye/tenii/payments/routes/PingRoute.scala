package com.ogunleye.tenii.payments.routes

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import akka.pattern.CircuitBreaker
import com.typesafe.scalalogging.LazyLogging
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import io.swagger.annotations.{Api, ApiOperation, ApiResponse, ApiResponses}
import javax.ws.rs.Path

@Path("ping")
@Api(value = "/ping", description = "ping", produces = "application/json")
class PingRoute(implicit system: ActorSystem, breaker: CircuitBreaker) extends RequestDirectives with LazyLogging {

  def route: Route = pathPrefix("ping") {
    pingMe
  }

  @ApiOperation(httpMethod = "GET", response = classOf[String], value = "ping")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Ok")
  ))
  def pingMe: Route = {
    get {
      complete(Health("OK"))
    }
  }

  case class Health(status: String)
}
