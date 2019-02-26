package nl.viasalix.horarium.module.calvijncollege.cup.ui.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import nl.viasalix.horarium.module.calvijncollege.cup.R

class LoadingFragment : SetupFragment() {
    override var onDoneCallback: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calvijncollege_cup_setup_loading, container, false)
    }
}
