package org.beckn.one.sandbox.bap.client.discovery.services

import arrow.core.Either
import arrow.core.Either.Left
import arrow.core.Either.Right
import arrow.core.identity
import org.beckn.one.sandbox.bap.client.external.hasBody
import org.beckn.one.sandbox.bap.client.external.isAckNegative
import org.beckn.one.sandbox.bap.client.external.isInternalServerError
import org.beckn.one.sandbox.bap.client.external.provider.BppClientFactory
import org.beckn.one.sandbox.bap.client.shared.dtos.SearchCriteria
import org.beckn.one.sandbox.bap.client.shared.errors.bpp.BppError
import org.beckn.protocol.schemas.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils

@Service
class BppSearchService @Autowired constructor(
  private val bppServiceClientFactory: BppClientFactory,
) {
  private val log: Logger = LoggerFactory.getLogger(BppSearchService::class.java)

  fun search(bppUri: String, context: ProtocolContext, criteria: SearchCriteria)
          : Either<BppError, ProtocolAckResponse> {
    return Either.catch {
      log.info("Invoking Search API on BPP: {}", bppUri)
      val bppServiceClient = bppServiceClientFactory.getClient(bppUri)
      log.info("Initiated Search for context: {}", context)
      val httpResponse = bppServiceClient.search(
        ProtocolSearchRequest(
          context,
          ProtocolSearchRequestMessage(
            ProtocolIntent(
              item = ProtocolIntentItem(descriptor = ProtocolIntentItemDescriptor()),
              provider = ProtocolProvider(id = criteria.providerId, category_id = criteria.categoryId, descriptor = ProtocolDescriptor(code = criteria.providerCode, name = "")),
              fulfillment = getFulfillmentFilter(criteria),
              category = ProtocolCategory(id = criteria.categoryId,descriptor = ProtocolDescriptor(name = ""))
            )
          )
        )
      ).execute()

      log.info("Search response. Status: {}, Body: {}", httpResponse.code(), httpResponse.body())
      return when {
        httpResponse.isInternalServerError() -> Left(BppError.Internal)
        !httpResponse.hasBody() -> Left(BppError.NullResponse)
        httpResponse.isAckNegative() -> Left(BppError.Nack)
        else -> {
          log.info("Successfully invoked search on Bpp. Response: {}", httpResponse.body())
          Right(httpResponse.body()!!)
        }
      }
    }.mapLeft {
      log.error("Error when initiating search", it)
      BppError.Internal
    }
  }

  private fun getFulfillmentFilter(criteria: SearchCriteria) =
    when {
      StringUtils.hasText(criteria.deliveryLocation) ->
        ProtocolFulfillment(
          start = ProtocolFulfillmentStart(location = ProtocolLocation(gps=criteria.pickupLocation), time = ProtocolTime(label = criteria.startTime)),
          end = ProtocolFulfillmentEnd(location = ProtocolLocation(gps = criteria.deliveryLocation), time = ProtocolTime(label = criteria.endTime)),
//          agent = ProtocolPerson(
//            name = criteria.doctorName,
//            id = criteria.doctorId,
//            tags = mapOf("@abdm/gov/in/system_of_med" to if (criteria.systemOfMedicine.isNullOrEmpty()) "" else criteria.systemOfMedicine,
//              "@abdm/gov/in/med_speciality" to if (criteria.medicineSpecialty.isNullOrEmpty()) "" else criteria.medicineSpecialty,
//              "@abdm/gov/in/spoken_langs" to if (criteria.spokenLanguage.isNullOrEmpty()) "" else criteria.spokenLanguage)
//          ),
          type = criteria.type,
          person = ProtocolPerson(
            descriptor = ProtocolDescriptor(name = criteria.searchString)
          )
        )
      else -> null
    }
}

