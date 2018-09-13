package nl.viasalix.horarium.ui.main.appointment

import androidx.recyclerview.widget.DiffUtil
import nl.viasalix.horarium.zermelo.model.Appointment

class AppointmentDiffCallback(private val oldList: List<Appointment>, private val newList: List<Appointment>) :
    DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size
    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        newList[newItemPosition].appointmentInstance == oldList[oldItemPosition].appointmentInstance

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        newList[newItemPosition] == oldList[oldItemPosition]
}