package nl.viasalix.horarium.module.calvijncollege.cup.ui.setup

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import nl.viasalix.horarium.module.calvijncollege.cup.R
import org.jetbrains.anko.sdk27.coroutines.onKey
import org.jetbrains.anko.sdk27.coroutines.textChangedListener

class SetupStep3 : SetupFragment() {
    override var onDoneCallback: (() -> Unit)? = null

    companion object {
        const val TAG: String = "HOR/CC/SETUP/STEP3"
    }

    private var pin = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calvijncollege_cup_setup_step3, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tv = view.findViewById<EditText>(R.id.module_calvijncollege_cup_setup_step3_pin)
        tv.onKey { _, keyCode, event ->
            if (event?.action == KeyEvent.ACTION_DOWN &&
                    keyCode == KeyEvent.KEYCODE_ENTER &&
                    tv.text.length == 4) {
                onDoneCallback?.invoke()
            } else {
                pin = tv.text.toString()
            }
        }
    }

    override fun onAttach(context: Context?) {
        if (context != null && context is CalvijnCollegeCUPSetup) {
            context.setNextHandler {
                context.pin = pin
            }
        }

        super.onAttach(context)
    }
}
