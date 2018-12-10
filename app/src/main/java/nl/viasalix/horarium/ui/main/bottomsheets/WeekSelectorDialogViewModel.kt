package nl.viasalix.horarium.ui.main.bottomsheets

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nl.viasalix.horarium.utils.DateUtils.getCurrentWeek
import nl.viasalix.horarium.utils.DateUtils.getCurrentYear
import java.lang.NumberFormatException

class WeekSelectorDialogViewModel : ViewModel() {
    val year = MutableLiveData<String>()
    val week = MutableLiveData<String>()
    val dayString = MutableLiveData<String>()
    val dateString = MutableLiveData<String>()

    fun decrementYear() {
        year.value = sumString(year.value ?: getCurrentYear().toString(), -1)
    }

    fun incrementYear() {
        year.value = sumString(year.value ?: getCurrentYear().toString(), +1)
    }

    fun decrementWeek() {
        week.value = sumString(week.value ?: getCurrentWeek().toString(), -1)
    }

    fun incrementWeek() {
        week.value = sumString(week.value ?: getCurrentWeek().toString(), +1)
    }

    fun resetToCurrent() {
        this.year.value = getCurrentYear().toString()
        this.week.value = getCurrentWeek().toString()
    }
}
