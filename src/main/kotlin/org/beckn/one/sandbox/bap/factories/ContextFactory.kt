package org.beckn.one.sandbox.bap.factories

import org.beckn.protocol.schemas.ProtocolContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Clock

@Component
class ContextFactory @Autowired constructor(
  @Value("\${context.domain_consultation}") private val domain_consultation: String,
  @Value("\${context.domain_diagnostics}") private val domain_diagnostics: String,
  @Value("\${context.city}") private val city: String,
  @Value("\${context.country}") private val country: String,
  @Value("\${context.bap_id}") private val bapId: String,
  @Value("\${context.bap_uri}") private val bapUrl: String,
  private val uuidFactory: UuidFactory,
  private val clock: Clock = Clock.systemUTC(),
  private val diagnostics: String = "diagnostics"
) {
  fun create(
    transactionId: String = uuidFactory.create(),
    messageId: String = uuidFactory.create(),
    action: ProtocolContext.Action? = ProtocolContext.Action.SEARCH,
    bppId: String? = null,
    domain: String? = null
  ) = ProtocolContext(
    domain = domain?:domain_consultation,
    country = country,
    city = city,
    action = action,
    coreVersion = ProtocolVersion.V0_7_1.value,
    bapId = bapId,
    bapUri = bapUrl,
    bppId = bppId,
    transactionId = transactionId,
    messageId = messageId,
    clock = clock,
  )
}