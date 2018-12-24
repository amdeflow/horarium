package nl.viasalix.horarium.module.calvijncollege.cup.ui

import android.content.Context
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import nl.viasalix.horarium.module.calvijncollege.cup.R

class SetupStep3 : Fragment() {

    private lateinit var viewModel: SetupStep3ViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calvijncollege_cup_setup_step3, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SetupStep3ViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onAttach(context: Context?) {
        if (context != null && context is CalvijncollegeCupSetup) {
            // TODO: Attach to parent: register Next callback or sth like that
        }

        super.onAttach(context)
    }
}
