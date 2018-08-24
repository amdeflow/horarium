package nl.viasalix.horarium.ui.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nl.viasalix.horarium.zermelo.ZermeloInstance

class LoginViewModel : ViewModel() {
    var schoolName: String = ""
    var authCode: String = ""
    var accessToken: String = ""

    val scanQr = MutableLiveData<Boolean>()

    private val _loggedIn = MutableLiveData<Boolean>()
    val loggedIn: LiveData<Boolean>
        get() = _loggedIn

    fun tryLogin() {
        val schoolNameMatches = schoolName.isNotEmpty()
        val authCodeMatches = authCode.length == 12
        if (schoolNameMatches && authCodeMatches) {
            ZermeloInstance(schoolName).tryLogin(authCode) { success, newAccessToken ->
                accessToken = newAccessToken
                _loggedIn.value = success
            }

            return
        }
    }

    fun scanQr() {
        scanQr.value = true
    }
}