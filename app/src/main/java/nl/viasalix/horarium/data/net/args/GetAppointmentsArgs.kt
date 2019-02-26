package nl.viasalix.horarium.data.net.args

import java.util.Date

data class GetAppointmentsArgs(
        val from: Date? = null,
        val till: Date? = null,
        val modifiedSince: Date? = null,
        val valid: Boolean = true,
        val cancelled: Boolean? = null,
        val includeHidden: Boolean = false
)
