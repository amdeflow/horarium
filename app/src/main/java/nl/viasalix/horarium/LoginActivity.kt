package nl.viasalix.horarium

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.zxing.integration.android.IntentIntegrator
import nl.viasalix.horarium.databinding.LoginActivityBinding
import nl.viasalix.horarium.ui.login.LoginQr
import nl.viasalix.horarium.ui.login.LoginViewModel
import nl.viasalix.horarium.utils.*
import nl.viasalix.horarium.utils.Constants.SP_KEY_ACCESS_TOKEN
import nl.viasalix.horarium.utils.Constants.SP_KEY_CODE
import nl.viasalix.horarium.utils.Constants.SP_KEY_CURRENT_USER
import nl.viasalix.horarium.utils.Constants.SP_KEY_FIRST_NAME
import nl.viasalix.horarium.utils.Constants.SP_KEY_LAST_NAME
import nl.viasalix.horarium.utils.Constants.SP_KEY_PREFIX
import nl.viasalix.horarium.utils.Constants.SP_KEY_SCHOOL_NAME
import nl.viasalix.horarium.utils.Constants.SP_KEY_USERS
import nl.viasalix.horarium.utils.Constants.SP_KEY_USER_IDENTIFIER
import nl.viasalix.horarium.utils.SharedPreferencesUtils.getUserSharedPreferences
import nl.viasalix.horarium.utils.SharedPreferencesUtils.makeUserId
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast

class LoginActivity : AppCompatActivity() {
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)
        viewModel.loggedIn.observe(this, Observer { loggedIn ->
            if (loggedIn == true) {
                onLoginDone()
            } else {
                runOnUiThread {
                    toast(getString(R.string.error_login))
                }
            }
        })
        viewModel.scanQr.observe(this, Observer { scanQr ->
            if (scanQr == true) {
                runOnUiThread {
                    val intentIntegrator = IntentIntegrator(this)
                    intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                    intentIntegrator.initiateScan()
                }
            }
        })

        val binding = DataBindingUtil.setContentView<LoginActivityBinding>(
            this, R.layout.login_activity
        )
        binding.viewModel = viewModel

        // Set the focus on the 'school name' field
        binding.root.findViewById<EditText>(R.id.schoolName).requestFocus()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            viewModel.scanQr.value = false

            if (result.contents != null) {
                try {
                    val qrContents = Gson().fromJson(result.contents, LoginQr::class.java)
                    viewModel.schoolName = qrContents.institution
                    viewModel.authCode = qrContents.code

                    viewModel.tryLogin()
                } catch (_: JsonParseException) {
                    longToast(getString(R.string.error_recognizing_qr_code))
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun onLoginDone() {
        val userId = makeUserId(viewModel.authCode)

        val userSp = getUserSharedPreferences(userId, this)
        userSp.edit(commit = true) {
            putString(SP_KEY_ACCESS_TOKEN, viewModel.accessToken)
            putString(SP_KEY_SCHOOL_NAME, viewModel.schoolName)
            putString(SP_KEY_USER_IDENTIFIER, userId)
            putString(SP_KEY_CODE, viewModel.user?.code)
            putString(SP_KEY_FIRST_NAME, viewModel.user?.firstName)
            putString(SP_KEY_PREFIX, viewModel.user?.prefix)
            putString(SP_KEY_LAST_NAME, viewModel.user?.lastName)
        }

        // Must use .toMutableSet() because default value can be an immutable set
        val users = defaultSharedPreferences.getStringSet(
                SP_KEY_USERS,
                emptySet()
        )!!.toMutableSet()
        users.add(viewModel.authCode)

        defaultSharedPreferences.edit(commit = true) {
            putStringSet(SP_KEY_USERS, users)
            putString(SP_KEY_CURRENT_USER, userId)
        }

        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
