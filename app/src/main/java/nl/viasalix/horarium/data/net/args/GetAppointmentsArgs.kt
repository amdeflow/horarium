package nl.viasalix.horarium.data.net.args

import nl.viasalix.horarium.utils.DateUtils
import nl.viasalix.horarium.utils.DateUtils.getCurrentYear
import java.util.Date

data class GetAppointmentsArgs(
        val from: Date? = null,
        val till: Date? = null,
        val modifiedSince: Date? = null,
        val valid: Boolean = true,
        val cancelled: Boolean? = null,
        val includeHidden: Boolean = false
) {
    companion object {
         fun inWeek(week: Int, year: Int = getCurrentYear()): GetAppointmentsArgs {
             return GetAppointmentsArgs(
                     from = DateUtils.startOfWeek(week, year),
                     till = DateUtils.endOfWeek(week, year)
             )
         }
    }
}