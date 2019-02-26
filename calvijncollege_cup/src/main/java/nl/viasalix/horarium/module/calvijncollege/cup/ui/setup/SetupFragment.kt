package nl.viasalix.horarium.module.calvijncollege.cup.ui.setup

import androidx.fragment.app.Fragment

abstract class SetupFragment : Fragment() {
    abstract var onDoneCallback: (() -> Unit)?
}
