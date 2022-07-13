package org.beckn.one.sandbox.bap.client.shared.dtos

import org.beckn.protocol.schemas.Default

data class SearchRequestDto @Default constructor(
  val context: ClientContext,
  val message: SearchRequestMessageDto,
)

data class SearchRequestMessageDto @Default constructor(
  val criteria: SearchCriteria
)

data class SearchCriteria @Default constructor(
  val searchString: String? = null,
  val deliveryLocation: String? = null,
  val providerId: String? = null,
  val categoryId: String? = null,
  val pickupLocation: String? = null,
  val providerName: String? = null,
  val categoryName: String? = null,
  val symptoms: String? = null,
  val domain: String? = null,
  val doctorId: String? = null,
  val doctorName: String? = null,
  val systemOfMedicine: String? = null,
  val medicineSpecialty: String? = null,
  val spokenLanguage: String? = null,
  val providerCode: String? = null,
  val startTime: String? = null,
  val endTime: String? = null,
  val type: String? = null,


  )
