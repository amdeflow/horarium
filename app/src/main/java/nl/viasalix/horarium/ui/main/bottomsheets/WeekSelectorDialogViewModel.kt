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

    private fun sumString(str: String, delta: Int): String {
        return if (str.isNotEmpty()) {
            try {
                (str.toInt() + delta).toString()
            } catch (e: NumberFormatException) {
                str
            }
        } else {
            str
        }
    }

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

    fun correctWeekAndYear() {
        if (year.value == "") year.value = getCurrentYear().toString()
        if (week.value == "") week.value = getCurrentWeek().toString()

        val week = try {
            (week.value ?: getCurrentWeek().toString()).toInt()
        } catch (e: NumberFormatException) {
            week.value = getCurrentWeek().toString()
            (week.value ?: getCurrentWeek().toString()).toInt()
        }
        val year = try {
            (year.value ?: getCurrentYear().toString()).toInt()
        } catch (e: NumberFormatException) {
            this.year.value = getCurrentYear().toString()
            (year.value ?: getCurrentYear().toString()).toInt()
        }
        if (year < 1970) this.year.value = 1970.toString()
        if (week < 1) this.week.value = 1.toString()
        if (week > 53) this.week.value = 52.toString()
    }
}
