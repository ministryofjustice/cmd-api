package uk.gov.justice.digital.hmpps.cmd.api.utils

import java.util.concurrent.atomic.AtomicInteger


private val counter = AtomicInteger()

fun uniqueLduCode(): String = "ABC_X_${counter.incrementAndGet()}"
