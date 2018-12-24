package nl.viasalix.horarium.ui.main.bottomsheets

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nl.viasalix.horarium.utils.DateUtils.getCurrentWeek
import nl.viasalix.horarium.utils.DateUtils.getCurrentYear

class WeekSelectorDialogViewModel(year: Int, week: Int) : ViewModel() {
    val year = MutableLiveData<Int>()
    val week = MutableLiveData<Int>()
    val dayString = MutableLiveData<String>()
    val dateString = MutableLiveData<String>()

    init {
        this.year.value = year
        this.week.value = week
    }

    fun decrementYear() {
        year.value = (year.value ?: getCurrentYear()) - 1
    }

    fun incrementYear() {
        year.value = (year.value ?: getCurrentYear()) + 1
    }

    fun decrementWeek() {
        week.value = (week.value ?: getCurrentWeek()) - 1
    }

    fun incrementWeek() {
        week.value = (week.value ?: getCurrentWeek()) + 1
    }

    fun resetToCurrent() {
        this.year.value = getCurrentYear()
        this.week.value = getCurrentWeek()
    }
}
