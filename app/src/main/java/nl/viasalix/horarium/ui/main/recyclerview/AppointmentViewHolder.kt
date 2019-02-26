package nl.viasalix.horarium.ui.main.recyclerview

import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.list_item_appointment.view.*
import nl.viasalix.horarium.R
import nl.viasalix.horarium.data.ChipStub
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

    fun addBuiltinChips(context: Context, appointment: Appointment) {
        val c = Chip(context)
        c.setTextColor(Color.WHITE)

        when {
            appointment.cancelled -> {
                c.chipBackgroundColor = ContextCompat.getColorStateList(context, R.color.chip_cancelled)
                c.chipIcon = ContextCompat.getDrawable(context, R.drawable.ic_twotone_remove_circle_24dp)
                c.chipIconTint = ContextCompat.getColorStateList(context, android.R.color.white)
                c.text = context.getString(R.string.cancelled)
            }
            appointment.moved -> {
                c.chipBackgroundColor = ContextCompat.getColorStateList(context, R.color.chip_moved)
                c.chipIcon = ContextCompat.getDrawable(context, R.drawable.ic_arrow_forward_black_24dp)
                c.chipIconTint = ContextCompat.getColorStateList(context, android.R.color.white)
                c.text = context.getString(R.string.moved)
            }
            appointment.modified -> {
                c.chipBackgroundColor = ContextCompat.getColorStateList(context, R.color.chip_modified)
                c.chipIcon = ContextCompat.getDrawable(context, R.drawable.ic_edit_black_24dp)
                c.chipIconTint = ContextCompat.getColorStateList(context, android.R.color.white)
                c.text = context.getString(R.string.modified)
            }
            else -> return
        }
        itemView.cgInfo.addView(c)
    }

    fun addExtraChips(context: Context, appointment: Appointment, chips: List<ChipStub>) {
        for (chip in chips) {
            val c = Chip(context)

            c.text = chip.name
            c.setOnClickListener(chip.action)
        }
    }

}
