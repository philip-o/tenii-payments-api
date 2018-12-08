package com.ogunleye.tenii.payments.routes

import akka.http.scaladsl.server.{Directive1, Directives, PathMatcher1}

trait RequestDirectives extends Directives {

  val potSegment: PathMatcher1[String] = Segment
  val userIdSegment: PathMatcher1[String] = Segment


  //val prebookingTokenDirective = parameter("preBookingToken".?).map(_.map(PreBookingToken))

  // def validateRequest(req: BookRequest): Directive0 = {
  //
  //    val result = (isValidArrivalDate(req.body.arrivalDate) |@| isValidDuration(req.body.duration)
  //      |@| isValidForename(req.body.leadGuest.firstName) |@| isValidSurname(req.body.leadGuest.lastName)
  //      |@| isValidRoomGuests(req.body) |@| isValidEmail(req.body.leadGuest.email)
  //      |@| isValidPhoneNumber(req.body.leadGuest.phone) |@| isValidBillingAddress(req.body)
  //      |@| isValidTrains(req.body.trains) |@| isValidMembershipNumber(req.body.leadGuest.membershipNumber))
  //      .map(_ + _ + _ + _ + _ + _ + _ + _ + _ + _)
  //
  //    result match {
  //      case Valid(_) => pass
  //      case Invalid(_) => reject(RequestValidationRejection(ErrorResponseList(toListOfErrorResponse(result))))
  //    }
  //
  //  }

}
