package org.neteinstein.pickaname.domain.time

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Wall-clock "now" in epoch millis, for the use cases that stamp/compare refresh timestamps.
 *
 * Replaces the `System::currentTimeMillis` method references those use cases defaulted to, which
 * are JVM-only and so broke this module's wasmJs target. Callers that need determinism (every
 * test) keep passing their own `() -> Long` instead, exactly as before.
 */
@OptIn(ExperimentalTime::class)
internal fun systemCurrentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()
