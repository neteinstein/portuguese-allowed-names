package org.neteinstein.pickaname.domain.model

/**
 * How often the app should automatically re-download and re-parse the names source. [days] is
 * the elapsed time after which an automatic refresh becomes due; [DEFAULT] is what ships out of
 * the box.
 */
enum class RefreshPeriod(val days: Int) {
    WEEKLY(7),
    MONTHLY(30),
    QUARTERLY(91),
    BI_YEARLY(182),
    YEARLY(365);

    /** [days] expressed in milliseconds, for comparing against elapsed wall-clock time. */
    val durationMillis: Long get() = days * MILLIS_PER_DAY

    companion object {
        val DEFAULT: RefreshPeriod = YEARLY
        private const val MILLIS_PER_DAY: Long = 24L * 60 * 60 * 1000
    }
}
