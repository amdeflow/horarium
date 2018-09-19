package nl.viasalix.horarium.zermelo.args

import nl.viasalix.horarium.zermelo.model.Appointment
import nl.viasalix.horarium.zermelo.utils.DateUtils
import java.util.Date

data class GetAppointmentsArgs(
    val week: Int,
    val from: Date? = DateUtils.startOfWeek(week),
    val till: Date? = DateUtils.endOfWeek(week),
    val modifiedSince: Date? = null,
    val valid: Boolean? = null,
    val cancelled: Boolean? = null,
    val includeHidden: Boolean? = null,
    val user: String = "~me",
    val callback: (List<Appointment>?) -> Unit
)