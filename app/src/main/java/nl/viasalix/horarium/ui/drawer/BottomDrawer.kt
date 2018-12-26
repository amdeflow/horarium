package nl.viasalix.horarium.ui.drawer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.navigation.NavigationView
import nl.viasalix.horarium.MainActivity
import nl.viasalix.horarium.R
import nl.viasalix.horarium.events.args.ContextEventArgs
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.support.v4.runOnUiThread

class BottomDrawer : BottomSheetDialogFragment() {

    private val callbacks: MutableList<() -> Unit> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.bottom_drawer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val navigationView = view.findViewById<NavigationView>(R.id.bottom_drawer_navigationView)
        val menu = navigationView.menu

        navigationView.setNavigationItemSelectedListener { item ->
            val itemId = item.itemId

            when (itemId) {
                R.id.drawer_settings -> {}
                R.id.drawer_announcements -> {}
                R.id.drawer_schedule -> {}
                else ->
                    if (itemId >= 0 && itemId < callbacks.size) {
                        callbacks[itemId].invoke()
                    }
            }

            dismiss()

            true
        }


        doAsync {
            var i = 0

            val mainActivity = context
            if (mainActivity != null && mainActivity is MainActivity) {
                val result = mainActivity.userEvents.provideMainDrawerMenuItems.invoke(ContextEventArgs(mainActivity))
                result.forEach { menuItems ->
                    for ((text, callback) in menuItems) {
                        runOnUiThread {
                            menu.add(R.id.drawer_group_modules, i, Menu.NONE, text)
                        }

                        callbacks.add(callback)
                        i++
                    }
                }
            }
        }
    }
}