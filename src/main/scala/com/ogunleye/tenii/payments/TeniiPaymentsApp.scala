package com.ogunleye.tenii.payments

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.RouteConcatenation
import akka.pattern.CircuitBreaker
import akka.stream.ActorMaterializer
import com.ogunleye.tenii.payments.routes.{PaymentRoute, PingRoute, PotRoute}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration._
import scala.util.Properties

object TeniiApp extends App with LazyLogging with RouteConcatenation {

  val applicationName = "tenii-payments"

  implicit val system = ActorSystem(applicationName)
  implicit val mat = ActorMaterializer()

  logger.info(s"Number of processors visible to $applicationName service: ${Runtime.getRuntime.availableProcessors()}")

  implicit val breaker = CircuitBreaker(system.scheduler, 20, 100 seconds, 12 seconds)

  // routes
  //val swaggerDocRoute = new SwaggerDocRoute().routes
  val paymentRoute = new PaymentRoute().route
  val potRoute = new PotRoute().route
  val pingRoute = new PingRoute().route

  val routes = paymentRoute ~ potRoute ~ pingRoute

  val port = Properties.envOrElse("PORT", "8080").toInt
  Http().bindAndHandle(routes, "0.0.0.0", port)
  logger.info(s"$applicationName application started on port $port")

}
