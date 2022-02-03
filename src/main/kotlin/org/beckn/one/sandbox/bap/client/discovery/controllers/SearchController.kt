package org.beckn.one.sandbox.bap.client.discovery.controllers

import org.beckn.one.sandbox.bap.client.discovery.services.SearchService
import org.beckn.one.sandbox.bap.client.shared.dtos.SearchRequestDto
import org.beckn.one.sandbox.bap.factories.ContextFactory
import org.beckn.protocol.schemas.ProtocolAckResponse
import org.beckn.protocol.schemas.ResponseMessage
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class SearchController @Autowired constructor(
  val searchService: SearchService,
  val contextFactory: ContextFactory,
  @Value("\${context.domain_consultation}") private val domain_consultation: String,
  @Value("\${context.domain_diagnostics}") private val domain_diagnostics: String,
) {
  val log: Logger = LoggerFactory.getLogger(this::class.java)

  @PostMapping("/client/v1/search")
  @ResponseBody
  fun searchV1(@RequestBody request: SearchRequestDto): ResponseEntity<ProtocolAckResponse> {
    val domain = if(!request.message.criteria.domain.isNullOrEmpty() && request.message.criteria.domain == "diagnostics") domain_diagnostics else domain_consultation
    val protocolContext =
      contextFactory.create(transactionId = request.context.transactionId, bppId = request.context.bppId, domain = domain)
    return searchService.search(protocolContext, request.message.criteria)
      .fold(
        {
          log.error("Error during search. Error: {}", it)
          ResponseEntity
            .status(it.status().value())
            .body(ProtocolAckResponse(protocolContext, it.message(), it.error()))
        },
        {
          log.info("Successfully initiated Search")
          ResponseEntity.ok(ProtocolAckResponse(protocolContext, ResponseMessage.ack()))
        }
      )
  }
}