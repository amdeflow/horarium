package nl.viasalix.horarium.ui.main.dialogs

import android.app.AlertDialog
import android.content.Context
import android.text.InputType
import android.view.ContextThemeWrapper
import android.widget.EditText
import nl.viasalix.horarium.R

object CustomWeekDialog {
    fun show(context: Context?, onDoneCallback: (Boolean, Int) -> Unit) {
        val builder = AlertDialog.Builder(ContextThemeWrapper(context, R.style.ThemeOverlay_MaterialComponents_Dialog))
        builder.setTitle("Enter a week number")

        val input = EditText(context)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        input.maxWidth = 2

        builder.setView(input)

        builder.setPositiveButton(context?.getString(android.R.string.ok)) { _, _ ->
            if (input.text.toString().toInt() >= 0)
                onDoneCallback(true, input.text.toString().toInt())
            else
                show(context, onDoneCallback)
        }

        builder.setNegativeButton(context?.getString(android.R.string.cancel)) { dialog, _ ->
            dialog.cancel()

            onDoneCallback(false, -1)
        }

        builder.show()
    }
}