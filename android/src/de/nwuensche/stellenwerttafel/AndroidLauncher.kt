package de.nwuensche.stellenwerttafel

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.style.TextAppearanceSpan
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.resources.TextAppearance
import org.w3c.dom.Text
import kotlin.math.roundToInt


class AndroidLauncher : AndroidApplication() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val config = AndroidApplicationConfiguration()
        val main = MyGdxGame()


        val builder = MaterialAlertDialogBuilder(this)
        builder.setCancelable(false) //Cant press next to dialog to skip it
        //builder.setTitle("Androidly Alert")
        //builder.setMessage("We have a message")
//builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))
        builder.setPositiveButton("1000") { dialog, which ->
            Toast.makeText(applicationContext,
                    android.R.string.yes, Toast.LENGTH_SHORT).show()
        }

        builder.setNegativeButton("100") { dialog, which ->
            Toast.makeText(applicationContext,
                    android.R.string.no, Toast.LENGTH_SHORT).show()
        }

        builder.setNeutralButton("10") { dialog, which -> // 10 has largest button, because smallest kids will use it
            Toast.makeText(applicationContext,
                    "Maybe", Toast.LENGTH_SHORT).show()
            main.board.titleTable100Number = 2 // This would probably work when Applciation would not show behind dialog

        }
        val density = resources.displayMetrics.density
        val dialog = builder.create()

        dialog.show()

        //Enlarge Size Fonts for better pressing
        //Should !really! adjust after show
        for (button in listOf(dialog.getButton(DialogInterface.BUTTON_POSITIVE), dialog.getButton(DialogInterface.BUTTON_NEUTRAL), dialog.getButton(DialogInterface.BUTTON_NEGATIVE)))
        {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) { // TODO Dont I have this?
                button.setTextAppearance(R.style.TextAppearance_AppCompat_Large)
            } else {
                button.setTextAppearance(this, R.style.TextAppearance_AppCompat_Large)
            }
        }

        val fontSize = dialog.getButton(DialogInterface.BUTTON_POSITIVE).textSize
        //Need to adjust width, otherwise large gap between left and middle button
        //Should !really! adjust after show
        dialog.window?.setLayout((8*fontSize*density).roundToInt(), (3*fontSize*density).roundToInt()); //Controlling width and height.

        initialize(main, config)
    }
}