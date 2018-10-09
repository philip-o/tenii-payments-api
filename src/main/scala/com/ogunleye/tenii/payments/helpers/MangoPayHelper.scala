package com.ogunleye.tenii.payments.helpers

import java.time.{ ZoneId, ZoneOffset }
import java.util.{ ArrayList, Calendar, TimeZone }

import com.mangopay.core.{ Address, Money }
import com.mangopay.core.enumerations.{ BankAccountType, CountryIso, CultureCode, CurrencyIso }
import com.mangopay.entities.subentities.{ BankAccountDetailsGB, PayInExecutionDetailsDirect, PayInPaymentDetailsDirectDebit }
import com.mangopay.entities._
import com.ogunleye.tenii.payments.model.api.{ CreateTeniiPaymentUserRequest, PaymentDestinationAccount, PaymentSourceAccount }
import com.ogunleye.tenii.payments.model.db.{ TeniiBankAccount, TeniiPot, TeniiUser }

trait MangoPayHelper {

  implicit def createUser(request: CreateTeniiPaymentUserRequest): User = {
    val user = new UserNatural()
    user.FirstName = request.firstName
    user.LastName = request.surname
    user.Email = request.email
    user.Address = request
    user.Birthday = request
    user.Nationality = CountryIso.GB
    user.CountryOfResidence = CountryIso.GB
    user
  }

  implicit def getNewAddress(request: CreateTeniiPaymentUserRequest): Address = {
    val result = new Address()
    result.AddressLine1 = request.address.addressLine1
    result.AddressLine2 = request.address.addressLine2.getOrElse("")
    result.City = request.address.city
    result.Country = CountryIso.GB
    result.PostalCode = request.address.postalCode
    result.Region = request.address.region
    result
  }

  implicit def createDOB(request: CreateTeniiPaymentUserRequest): Long = {
    val date = Calendar.getInstance()
    date.set(request.dob.year, request.dob.month - 1, request.dob.day)
    date.setTimeZone(TimeZone.getTimeZone(ZoneId.of(ZoneOffset.UTC.getId)))
    date.getTimeInMillis / 1000
  }

  implicit def createBankAccountDetailsGB(request: PaymentSourceAccount): BankAccountDetailsGB = {
    val details = new BankAccountDetailsGB()
    details.AccountNumber = request.accountNumber
    details.SortCode = request.sortCode
    details
  }

  implicit def createBankAccountDetailsGB(request: PaymentDestinationAccount): BankAccountDetailsGB = {
    val details = new BankAccountDetailsGB()
    details.AccountNumber = request.accountNumber
    details.SortCode = request.sortCode
    details
  }

  implicit def createBankAccount(request: CreateTeniiPaymentUserRequest, user: User): BankAccount = {
    val account = new BankAccount()
    account.Active = true
    account.Details = request.sourceAccount
    account.OwnerAddress = request
    account.OwnerName = s"${request.firstName} ${request.surname}"
    account.Type = BankAccountType.GB
    account.UserId = user.Id
    account
  }

  implicit def createProductBankAccount(request: CreateTeniiPaymentUserRequest, user: User): BankAccount = {
    val account = new BankAccount()
    account.Active = true
    account.Details = request.destinationAccount
    account.OwnerAddress = request
    account.OwnerName = s"${request.firstName} ${request.surname}"
    account.Type = BankAccountType.GB
    account.UserId = user.Id
    account
  }

  def createWallet(request: CreateTeniiPaymentUserRequest, user: User): Wallet = {
    val wallet = new Wallet()
    wallet.Owners = new ArrayList[String]()
    wallet.Owners.add(user.Id)
    wallet.Currency = CurrencyIso.GBP
    wallet.Description = "WALLET IN GBP"
    wallet
  }

  def createTeniiUser(request: CreateTeniiPaymentUserRequest, user: User, wallet: Wallet, account: BankAccount, mandate: Mandate, destinationBankAccount: BankAccount): TeniiUser = {
    TeniiUser(
      teniiId = request.teniiUserId,
      paymentUserId = user.Id,
      wallet = createTeniiPot(wallet),
      accounts = Some(List(createTeniiBankAccount(request, account))),
      mandateId = mandate.Id,
      destinationAccount = createTeniiBankAccount(request, destinationBankAccount)
    )
  }

  def createTeniiPot(wallet: Wallet): TeniiPot = {
    TeniiPot(
      id = wallet.Id
    )
  }

  def createTeniiBankAccount(request: CreateTeniiPaymentUserRequest, account: BankAccount): TeniiBankAccount = {
    TeniiBankAccount(
      account.Id,
      request.sourceAccount.accountNumber,
      request.sourceAccount.sortCode,
      request.sourceAccount.provider
    )
  }

  implicit def createMandate(request: CreateTeniiPaymentUserRequest, account: BankAccount): Mandate = {
    val mandate = new Mandate()
    mandate.BankAccountId = account.Id
    mandate.Culture = CultureCode.EN
    mandate.UserId = account.UserId
    mandate.ReturnURL = "http://tenii.com"
    mandate
  }

  implicit def createDirectDebit(user: TeniiUser, amount: Double = 1): PayIn = {

    val money = new Money()
    money.Amount = (amount * 100).toInt
    money.Currency = CurrencyIso.GBP
    val fees = new Money
    fees.Currency = CurrencyIso.GBP

    val payIn = new PayIn()
    payIn.AuthorId = user.paymentUserId
    payIn.DebitedFunds = money
    payIn.Fees = fees
    payIn.CreditedWalletId = user.wallet.id
    val paymentDetails = new PayInPaymentDetailsDirectDebit()
    paymentDetails.MandateId = user.mandateId
    payIn.PaymentDetails = paymentDetails
    val executionDetails = new PayInExecutionDetailsDirect()
    payIn.ExecutionDetails = executionDetails
    payIn
  }

  implicit def createTransferOut(user: TeniiUser, amount: Double): PayOut = {
    val money = new Money()
    money.Amount = (amount * 100).toInt
    money.Currency = CurrencyIso.GBP
    val fees = new Money
    fees.Currency = CurrencyIso.GBP

    val payout = new PayOut()
    payout.AuthorId = user.paymentUserId
    payout.CreditedUserId = user.paymentUserId

    payout.DebitedWalletId = user.wallet.id
    payout
  }
}
