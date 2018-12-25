package nl.viasalix.horarium.module.calvijncollege.cup.ui

import android.content.Context
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup

import nl.viasalix.horarium.module.calvijncollege.cup.R

class SetupStep2 : Fragment() {

    companion object {
        const val TAG: String = "HOR/CC/SETUP/STEP2"
    }

    var setup: CalvijncollegeCupSetup? = null

    var selectedIndex = 0

    var userKeys: List<String> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calvijncollege_cup_setup_step2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val selection = view.findViewById<RadioGroup>(R.id.module_calvijncollege_cup_setup_step2_usernameSelection)
        selection.setOnCheckedChangeListener { rg, index -> if (rg != null) {
            Log.d(TAG, "Selected index: $index")
            selectedIndex = index
        }}

        if (setup != null) {
            var first: RadioButton? = null

            userKeys = setup!!.availableUsers.keys.toList()

            // Add all the buttons to the RadioGroup
            for (text in setup!!.availableUsers.values) {
                val button = RadioButton(context)
                button.text = text

                if (first == null) first = button

                selection.addView(button)
            }

            // Check the first item
            first?.isChecked = true
        }
    }

    override fun onAttach(context: Context?) {
        if (context != null && context is CalvijncollegeCupSetup) {
            setup = context
            context.setNextHandler {
                context.selectedUser = userKeys[selectedIndex]
            }
        }

        super.onAttach(context)
    }
}
