package nl.viasalix.horarium.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nl.viasalix.horarium.zermelo.ZermeloInstance

class LoginViewModel : ViewModel() {
    var schoolName: String = ""
    var authCode: String = ""
    var accessToken: String = ""

    private val _loggedIn = MutableLiveData<Boolean>()
    val loggedIn: LiveData<Boolean>
        get() = _loggedIn

    fun onLoginClick() {
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
}