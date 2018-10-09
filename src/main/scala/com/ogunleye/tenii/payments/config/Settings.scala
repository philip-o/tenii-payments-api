package com.ogunleye.tenii.payments.config

import com.typesafe.config.{ Config, ConfigFactory }

object Settings {

  private[config] val config: Config = ConfigFactory.load()

  val clientId: String = config.getStringList("client.id").get(0)
  val clientPassword: String = config.getStringList("client.password").get(0)

}
