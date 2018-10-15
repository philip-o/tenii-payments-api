package com.ogunleye.tenii.payments.config

import com.typesafe.config.{Config, ConfigFactory}

import scala.util.Properties

object Settings {

  private[config] val config: Config = ConfigFactory.load()

  val clientId: String = Properties.envOrElse("CLIENT_ID", "")
  val clientPassword: String = Properties.envOrElse("CLIENT_PASSWORD", "")

}
