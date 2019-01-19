package com.ogunleye.tenii.payments.routes

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.github.swagger.akka.{HasActorSystem, SwaggerHttpService}

import scala.reflect.runtime.{universe => ru}

class SwaggerDocRoute(implicit system: ActorSystem, mat: ActorMaterializer) extends SwaggerHttpService with HasActorSystem {
  override implicit val actorSystem: ActorSystem = system
  override implicit val materializer: ActorMaterializer = mat
  override val apiTypes = Seq(ru.typeOf[PaymentRoute], ru.typeOf[PingRoute], ru.typeOf[PotRoute])
}
