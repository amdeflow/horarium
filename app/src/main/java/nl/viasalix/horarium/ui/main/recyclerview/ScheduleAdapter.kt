package nl.viasalix.horarium.ui.main.recyclerview

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.list_item_appointment.view.*
import nl.viasalix.horarium.R
import nl.viasalix.horarium.databinding.ListItemAppointmentBinding
import nl.viasalix.horarium.data.zermelo.model.Appointment
import nl.viasalix.horarium.utils.DateUtils

class ScheduleAdapter(private val context: Context) :
        ListAdapter<Appointment, AppointmentViewHolder>(AppointmentDiffCallback()) {

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val appointment = getItem(position)
        val newDay = if (position > 0) {
            DateUtils.isOtherDay(appointment.start, getItem(position - 1).start)
        } else {
            true
        }

        val dayString = if (newDay) {
            DateUtils.dayToString(context, appointment.start)
        } else {
            ""
        }

        holder.apply {
            bind(appointment)
            itemView.tag = appointment
            if (newDay) {
                itemView.tvDay.text = dayString
                itemView.tvDay.visibility = View.VISIBLE
            }
            if (appointment.cancelled) {
                val chip = Chip(context)
                chip.setTextColor(Color.WHITE)
                chip.chipBackgroundColor = ContextCompat.getColorStateList(context, R.color.chip_cancelled)
                chip.chipIcon = ContextCompat.getDrawable(context, R.drawable.ic_twotone_remove_circle_24dp)
                chip.chipIconTint = ContextCompat.getColorStateList(context, android.R.color.white)
                chip.text = context.getString(R.string.cancelled)
                itemView.cgInfo.addView(chip)
            }
            if (appointment.moved) {
                val chip = Chip(context)
                chip.setTextColor(Color.WHITE)
                chip.chipBackgroundColor = ContextCompat.getColorStateList(context, R.color.chip_moved)
                chip.chipIcon = ContextCompat.getDrawable(context, R.drawable.ic_arrow_forward_black_24dp)
                chip.chipIconTint = ContextCompat.getColorStateList(context, android.R.color.white)
                chip.text = context.getString(R.string.moved)
                itemView.cgInfo.addView(chip)
            }
            if (appointment.modified) {

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        return AppointmentViewHolder(ListItemAppointmentBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
    }
}
