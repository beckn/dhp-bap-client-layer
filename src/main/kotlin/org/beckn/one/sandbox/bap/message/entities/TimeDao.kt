package org.beckn.one.sandbox.bap.message.entities

import org.beckn.protocol.schemas.Default
import java.time.Instant

data class TimeDao @Default constructor(
  val label: String? = null,
  val timestamp: java.time.Instant? = null,
  val duration: String? = null,
  val range: TimeRangeDao? = null,
  val days: String? = null
)

data class TimeRangeDao @Default constructor(
  val start: Instant? = null,
  val end: Instant? = null
)


