package nl.viasalix.horarium.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nl.viasalix.horarium.zermelo.ZermeloInstance
import nl.viasalix.horarium.zermelo.model.User

class LoginViewModel : ViewModel() {
    var schoolName: String = ""
    var authCode: String = ""
    var accessToken: String = ""
    var user: User? = null

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

                ZermeloInstance(schoolName, accessToken).getCurrentUser { newUser ->
                    user = newUser
                }
            }

            return
        }
    }

    fun scanQr() {
        scanQr.value = true
    }
}