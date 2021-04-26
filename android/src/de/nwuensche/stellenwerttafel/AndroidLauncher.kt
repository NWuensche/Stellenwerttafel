package de.nwuensche.stellenwerttafel

import android.content.DialogInterface
import android.os.Bundle
import android.widget.Button
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class AndroidLauncher : AndroidApplication() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val config = AndroidApplicationConfiguration()
        val main = MyGdxGame()

        createAndShowNumColumnsDialog(main)

        initialize(main, config)
    }

    private fun createAndShowNumColumnsDialog(main: MyGdxGame) {
        val builder = MaterialAlertDialogBuilder(this) //Keep this instead of AlertDialog.Builder, because AlertDialog.Builder does not have Material Design and looks ugly
        builder.setCancelable(false) //Cant press next to dialog to skip it

        //Need to call findViewByID from this view, thus store it
        val buttonView = layoutInflater.inflate(R.layout.startdialog, null)
        builder.setView(buttonView)

        val dialog = builder.create()
        //Need to call findViewByID from this view, else will not find buttons
        buttonView.findViewById<Button>(R.id.button10).setOnClickListener {
            main.dialogFinished(2)
            dialog.dismiss()
        }
        buttonView.findViewById<Button>(R.id.button100).setOnClickListener {
            main.dialogFinished(3)
            dialog.dismiss()
        }
        buttonView.findViewById<Button>(R.id.button1000).setOnClickListener {
            main.dialogFinished(4)
            dialog.dismiss()
        }
        dialog.show()
    }
}