package nl.viasalix.horarium.ui.main.bottomsheets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class WeekSelectorDialogViewModelFactory(
        private val year: Int,
        private val week: Int
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>) = WeekSelectorDialogViewModel(year, week) as T
}
