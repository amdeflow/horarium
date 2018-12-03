package nl.viasalix.horarium.zermelo.args

import nl.viasalix.horarium.zermelo.model.Appointment
import nl.viasalix.horarium.utils.DateUtils
import nl.viasalix.horarium.utils.DateUtils.getCurrentYear
import java.util.Date

data class GetAppointmentsArgs(
        val week: Int,
        val from: Date = DateUtils.startOfWeek(week, getCurrentYear()),
        val till: Date = DateUtils.endOfWeek(week, getCurrentYear()),
        val modifiedSince: Date? = null,
        val valid: Boolean? = null,
        val cancelled: Boolean? = null,
        val includeHidden: Boolean? = null,
        val user: String = "~me",
        val callback: (List<Appointment>?, Date, Date) -> Unit
)