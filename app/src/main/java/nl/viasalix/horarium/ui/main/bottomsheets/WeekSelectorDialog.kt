package nl.viasalix.horarium.ui.main.bottomsheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import nl.viasalix.horarium.databinding.WeekSelectorDialogBinding
import nl.viasalix.horarium.utils.DateUtils.dateToString
import nl.viasalix.horarium.utils.DateUtils.dayToString
import nl.viasalix.horarium.utils.DateUtils.getCurrentWeek
import nl.viasalix.horarium.utils.DateUtils.getCurrentYear
import nl.viasalix.horarium.utils.DateUtils.startOfWeek
import java.util.*

class WeekSelectorDialog : BottomSheetDialogFragment() {

    private lateinit var viewModel: WeekSelectorDialogViewModel
    var onResultCallback: ((Int?, Int?) -> Unit)? = null
    var initYear: Int? = null
    var initWeek: Int? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val binding = WeekSelectorDialogBinding.inflate(inflater, container, false)
        viewModel = ViewModelProviders.of(this).get(WeekSelectorDialogViewModel::class.java)
        viewModel.year.value = initYear ?: getCurrentYear()
        viewModel.week.value = initWeek ?: getCurrentWeek()
        initObservers()

        binding.viewmodel = viewModel
        binding.setLifecycleOwner(this)

        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnOk.setOnClickListener {
            dismiss()
            onResultCallback?.invoke(viewModel.year.value, viewModel.week.value)
        }

        return binding.root
    }

    private fun initObservers() {
        viewModel.week.observe(this, Observer { week ->
            if (week > 52) viewModel.week.value = 52
            if (week < 0) viewModel.week.value = 0
            updateDate()
        })
        viewModel.year.observe(this, Observer { year ->
            if (year < 1970) viewModel.year.value = 1970
            updateDate()
        })
    }

    private fun updateDate() {
        val startOfWeek = Calendar.getInstance()
        startOfWeek.time = startOfWeek(
                viewModel.week.value ?: getCurrentWeek(),
                viewModel.year.value ?: getCurrentYear()
        )

        val dayString = dayToString(startOfWeek.time)
        val dateString = dateToString(context, startOfWeek.time)
        viewModel.dayString.value = dayString.capitalize()
        viewModel.dateString.value = dateString.capitalize()
    }

    fun setup(year: Int?, week: Int?) {
        initYear = year
        initWeek = week
    }

}
