package nl.viasalix.horarium

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import nl.viasalix.horarium.databinding.LoginActivityBinding
import nl.viasalix.horarium.ui.login.LoginViewModel
import org.jetbrains.anko.defaultSharedPreferences
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

        val binding = DataBindingUtil.setContentView<LoginActivityBinding>(
            this, R.layout.login_activity
        )
        binding.viewModel = viewModel
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
    }
}