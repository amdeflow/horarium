package nl.viasalix.horarium.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nl.viasalix.horarium.data.net.ZermeloApi
import nl.viasalix.horarium.data.zermelo.model.User
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

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
            doAsync {
                val response = ZermeloApi.getInstance(schoolName, "")
                        .login(authCode).execute()

                accessToken = response.body()?.accessToken ?: ""
                uiThread {
                    _loggedIn.value = response.isSuccessful
                }

                val currentUser = ZermeloApi.getInstance(schoolName, accessToken)
                        .getUser().execute()
                user = currentUser.body()?.response?.data?.get(0)
            }
        }
    }

    fun scanQr() {
        scanQr.value = true
    }
}