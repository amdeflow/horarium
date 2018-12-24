package nl.viasalix.horarium.module.calvijncollege.cup.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import nl.viasalix.horarium.module.calvijncollege.cup.R
import org.jetbrains.anko.sdk27.coroutines.textChangedListener

class SetupStep1 : Fragment() {

    companion object {
        const val TAG: String = "HOR/CC/SETUP/STEP1"
    }

    var firstLettersOfSurname = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calvijncollege_cup_setup_step1, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tv = view.findViewById<EditText>(R.id.module_calvijncollege_cup_setup_step1_firstLettersOfSurname)
        tv.textChangedListener {
            onTextChanged { _, _, _, _ ->
                firstLettersOfSurname = tv.text.toString()
                Log.d(TAG, "firstLettersOfSurname = $firstLettersOfSurname")
            }
        }
    }

    override fun onAttach(context: Context?) {
        if (context != null && context is CalvijncollegeCupSetup) {
            context.setNextHandler {
                Log.d(TAG, "Next handler of step 1 is executing")
                context.firstLettersOfSurname = firstLettersOfSurname
            }
        }

        super.onAttach(context)
    }
}
