package com.ogunleye.tenii.payments.model

case class TeniiDate(day: Int, month: Int, year: Int)

case class Address(addressLine1: String, addressLine2: Option[String] = None, city: String, country: String = "GB", postalCode: String, region: String)
