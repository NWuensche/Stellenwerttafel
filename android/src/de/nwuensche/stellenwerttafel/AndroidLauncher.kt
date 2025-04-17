package de.nwuensche.stellenwerttafel

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*


class AndroidLauncher : AndroidApplication() {
    private val PREFS_NAME = "StellenwerttafelPrefs"
    private val BACKGROUND_OPEN_COUNT_KEY = "backgroundOpenCount"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val config = AndroidApplicationConfiguration()
        val main = MyGdxGame()

        createAndShowNumColumnsDialog(main)

        initialize(main, config)
    }
    
    override fun onResume() {
        super.onResume()
        checkAndShowBackgroundDialog()
    }
    
    private fun checkAndShowBackgroundDialog() {
        // Check if current date is on or after May 10, 2025
        val currentDate = Calendar.getInstance()
        val targetDate = Calendar.getInstance().apply {
            set(2025, Calendar.MAY, 10, 0, 0, 0) // Month is 0-based in Calendar
            set(Calendar.MILLISECOND, 0)
        }
        
        // Only proceed if we're on or after the target date
        if (currentDate.timeInMillis < targetDate.timeInMillis) {
            return
        }
        
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var count = prefs.getInt(BACKGROUND_OPEN_COUNT_KEY, 0)
        
        // Increment the counter
        count++
        
        // Save the updated counter
        prefs.edit().putInt(BACKGROUND_OPEN_COUNT_KEY, count).apply()
        
        // Show dialog every third time
        if (count % 3 == 0) {
            showBackgroundDialog()
        }
    }
    
    private fun showBackgroundDialog() {
        runOnUiThread {
            val builder = MaterialAlertDialogBuilder(this)
            builder.setCancelable(false)
            builder.setMessage("Diese App kann leider ab dem 15.05.2025 nicht mehr aktualisiert werden. Lade dir stattdessen die (gleiche) kostenlose Stellenwerttafel aus dem Google Play Store")
            builder.setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            builder.setNeutralButton("Zur neuen App") { dialog, _ ->
                openPlayStoreLink()
                dialog.dismiss()
            }
            builder.create().show()
        }
    }
    
    private fun openPlayStoreLink() {
        val playStoreUri = Uri.parse("https://play.google.com/store/apps/developer?id=Niklas++Wünsche")
        
        // Try to open in Play Store app first
        val playStoreIntent = Intent(Intent.ACTION_VIEW, playStoreUri).apply {
            setPackage("com.android.vending") // Play Store package name
        }
        
        // Check if Play Store app is available
        if (playStoreIntent.resolveActivity(packageManager) != null) {
            startActivity(playStoreIntent)
        } else {
            // Fall back to browser if Play Store app is not available
            val browserIntent = Intent(Intent.ACTION_VIEW, playStoreUri)
            startActivity(browserIntent)
        }
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