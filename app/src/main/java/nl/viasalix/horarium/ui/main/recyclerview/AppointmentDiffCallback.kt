package nl.viasalix.horarium.ui.main.recyclerview

import androidx.recyclerview.widget.DiffUtil
import nl.viasalix.horarium.zermelo.model.Appointment

class AppointmentDiffCallback : DiffUtil.ItemCallback<Appointment>() {

    override fun areItemsTheSame(oldItem: Appointment, newItem: Appointment): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Appointment, newItem: Appointment): Boolean {
        return oldItem.start == newItem.start &&
                oldItem.end == newItem.end &&
                oldItem.startTimeSlot == newItem.startTimeSlot &&
                oldItem.endTimeSlot == newItem.endTimeSlot &&
                oldItem.subjects == newItem.subjects &&
                oldItem.teachers == newItem.teachers &&
                oldItem.locations == newItem.locations
    }
}