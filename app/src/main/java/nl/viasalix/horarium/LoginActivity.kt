package nl.viasalix.horarium

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
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
    private lateinit var view: View
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)
        viewModel.loggedIn.observe(this, Observer { loggedIn ->
            if (loggedIn == true) {
                onComplete()
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

        view = binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            viewModel.scanQr.value = false

            if (result.contents !== null) {
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

    private fun onComplete() {
        val userId = "user_${viewModel.authCode}"

        val userSp = getSharedPreferences(userId, Context.MODE_PRIVATE)
        userSp.edit(commit = true) {
            putString(getString(R.string.SP_KEY_ACCESS_TOKEN), viewModel.accessToken)
            putString(getString(R.string.SP_KEY_SCHOOL_NAME), viewModel.schoolName)
            putString(getString(R.string.SP_KEY_USER_IDENTIFIER), userId)
            putString(getString(R.string.SP_KEY_CODE), viewModel.user?.code)
            putString(getString(R.string.SP_KEY_FIRST_NAME), viewModel.user?.firstName)
            putString(getString(R.string.SP_KEY_PREFIX), viewModel.user?.prefix)
            putString(getString(R.string.SP_KEY_LAST_NAME), viewModel.user?.lastName)
        }

        // Must use .toMutableSet() because default value can be an immutable set
        val users = defaultSharedPreferences.getStringSet(getString(R.string.SP_KEY_USERS), emptySet())?.toMutableSet()
        users?.add(viewModel.authCode)

        defaultSharedPreferences.edit(commit = true) {
            putStringSet(getString(R.string.SP_KEY_USERS), users)
            putString(getString(R.string.SP_KEY_CURRENT_USER), userId)
        }

        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}