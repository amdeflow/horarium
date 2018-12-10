package nl.viasalix.horarium.ui.main.recyclerview

import androidx.recyclerview.widget.RecyclerView
import nl.viasalix.horarium.databinding.ListItemAppointmentBinding
import nl.viasalix.horarium.data.zermelo.model.Appointment

class AppointmentViewHolder(
        private val binding: ListItemAppointmentBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: Appointment) {
        binding.apply {
            appointment = item
            executePendingBindings()
        }
    }
}
