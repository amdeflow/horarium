package nl.viasalix.horarium.module.calvijncollege.cup

import android.content.Context
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class SetupStep1 : Fragment() {

    companion object {
        fun newInstance() = SetupStep1()
    }

    private lateinit var viewModel: SetupStep1ViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calvijncollege_cup_setup_step1, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SetupStep1ViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onAttach(context: Context?) {
        if (context != null && context is CalvijncollegeCupSetup) {
            // TODO: Attach to parent: register Next callback or sth like that
        }

        super.onAttach(context)
    }
}
