package nl.viasalix.horarium.module.calvijncollege.cup.ui

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import nl.viasalix.horarium.module.calvijncollege.cup.R

class SetupStep3 : Fragment() {

    companion object {
        fun newInstance() = SetupStep3()
    }

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
}
