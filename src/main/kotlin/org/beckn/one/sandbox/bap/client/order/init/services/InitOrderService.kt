package org.beckn.one.sandbox.bap.client.order.init.services

import arrow.core.Either
import arrow.core.flatMap
import com.google.gson.GsonBuilder
import org.beckn.one.sandbox.bap.client.shared.dtos.OrderDto
import org.beckn.one.sandbox.bap.client.shared.dtos.OrderItemDto
import org.beckn.one.sandbox.bap.client.shared.errors.CartError
import org.beckn.one.sandbox.bap.client.shared.services.RegistryService
import org.beckn.one.sandbox.bap.errors.HttpError
import org.beckn.protocol.schemas.ProtocolAckResponse
import org.beckn.protocol.schemas.ProtocolContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class InitOrderService @Autowired constructor(
  private val bppInitService: BppInitService,
  private val registryService: RegistryService,
  private val log: Logger = LoggerFactory.getLogger(InitOrderService::class.java)
) {
  fun initOrder(
    context: ProtocolContext,
    order: OrderDto
  ): Either<HttpError, ProtocolAckResponse?> {
    val gsonPretty = GsonBuilder().setPrettyPrinting().create()
    log.info("Got initialize order request. \nContext: {}, \nOrder: {}", gsonPretty.toJson(context), gsonPretty.toJson(order))
    if (order.items.isNullOrEmpty()) {
      log.info("Empty order received, no op. Order: {}", order)
      return Either.Right(null)
    }

    if (areMultipleBppItemsSelected(order.items)) {
      log.info("Order contains items from more than one BPP, returning error. Order: {}", order)
      return Either.Left(CartError.MultipleBpps)
    }

    if (areMultipleProviderItemsSelected(order.items)) {
      log.info("Order contains items from more than one provider, returning error. Cart: {}", order)
      return Either.Left(CartError.MultipleProviders)
    }

    return registryService.lookupBppById(order.items.first().bppId, context.domain)
      .flatMap {
        bppInitService.init(
          context,
          bppUri = it.first().subscriber_url,
          order = order
        )
      }
  }

  private fun areMultipleProviderItemsSelected(items: List<OrderItemDto>): Boolean =
    items.distinctBy { it.provider.id }.size > 1

  private fun areMultipleBppItemsSelected(items: List<OrderItemDto>): Boolean =
    items.distinctBy { it.bppId }.size > 1

}
