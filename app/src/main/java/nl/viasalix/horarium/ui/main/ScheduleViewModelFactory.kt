package nl.viasalix.horarium.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import nl.viasalix.horarium.data.repository.ScheduleRepository

class ScheduleViewModelFactory(
        private val repository: ScheduleRepository,
        private val year: Int,
        private val week: Int
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>) = ScheduleViewModel(repository, year, week) as T
}
