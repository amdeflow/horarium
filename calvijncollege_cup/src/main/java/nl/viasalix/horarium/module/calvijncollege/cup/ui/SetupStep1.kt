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
        fun newInstance() = SetupStep1()
    }

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
                Log.i("TEXT", tv.text.toString())
            }
        }
    }

    override fun onAttach(context: Context?) {
//        if (context != null && context is CalvijncollegeCupSetup) {
//            // TODO: Attach to parent: register Next callback or sth like that
//        }

        super.onAttach(context)
    }
}
