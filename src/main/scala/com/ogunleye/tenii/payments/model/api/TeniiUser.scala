package com.ogunleye.tenii.payments.model.api

import com.ogunleye.tenii.payments.model.{ Address, TeniiDate }

case class CreateTeniiPaymentUserRequest(
  teniiUserId: String,
  firstName: String,
  surname: String,
  email: String,
  address: Address,
  dob: TeniiDate,
  nationality: String = "GB",
  residence: String = "GB",
  sourceAccount: PaymentSourceAccount,
  destinationAccount: PaymentDestinationAccount
)

case class CreateTeniiPaymentUserResponse(teniiUserId: String, cause: Option[String] = None)

case class PaymentSourceAccount(sortCode: String, accountNumber: String, provider: String)

case class PaymentDestinationAccount(sortCode: String, accountNumber: String, provider: String)