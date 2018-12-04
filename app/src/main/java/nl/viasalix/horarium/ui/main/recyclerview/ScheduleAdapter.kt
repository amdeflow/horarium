package nl.viasalix.horarium.ui.main.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import nl.viasalix.horarium.databinding.ListItemAppointmentBinding
import nl.viasalix.horarium.data.zermelo.model.Appointment

class ScheduleAdapter : ListAdapter<Appointment, AppointmentViewHolder>(AppointmentDiffCallback()) {

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val appointment = getItem(position)
        holder.apply {
            bind(appointment)
            itemView.tag = appointment
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        return AppointmentViewHolder(ListItemAppointmentBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
    }
}