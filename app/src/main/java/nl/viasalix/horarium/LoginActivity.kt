package nl.viasalix.horarium

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
                onComplete()
            } else {
                runOnUiThread {
                    toast("Error while trying to log in")
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

            if (result.contents !== null) {
                try {
                    Log.v("LoginActivity r.c", result.contents)

                    val qrContents = Gson().fromJson(result.contents, LoginQr::class.java)
                    viewModel.schoolName = qrContents.institution
                    viewModel.authCode = qrContents.code

                    Log.v("LoginActivity vm", "trying to login with ${viewModel.schoolName} && ${viewModel.authCode}")

                    viewModel.tryLogin()
                } catch (_: JsonParseException) {
                    longToast("Error recognizing the QR code. Please try again")
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun onComplete() {
        val userName = "user_${viewModel.authCode}"

        val userSp = getSharedPreferences(userName, Context.MODE_PRIVATE)
        userSp.edit {
            putString(getString(R.string.SP_KEY_ACCESS_TOKEN), viewModel.accessToken)
            putString(getString(R.string.SP_KEY_SCHOOL_NAME), viewModel.schoolName)
        }

        val users = defaultSharedPreferences.getStringSet(getString(R.string.SP_KEY_USERS), emptySet())?.toMutableSet()
        users?.add(viewModel.authCode)

        defaultSharedPreferences.edit { putStringSet(getString(R.string.SP_KEY_USERS), users) }
        defaultSharedPreferences.edit { putString(getString(R.string.SP_KEY_CURRENT_USER), userName) }

        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}