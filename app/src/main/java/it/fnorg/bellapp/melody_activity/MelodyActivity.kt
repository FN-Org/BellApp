package it.fnorg.bellapp.melody_activity

import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import it.fnorg.bellapp.R
import it.fnorg.bellapp.calendar_activity.CalendarViewModel

class MelodyActivity : AppCompatActivity() {

    // Initialize MelodyViewModel and CalendarViewModel using Kotlin's viewModels delegate.
    private val melodyViewModel: MelodyViewModel by viewModels()
    private val calendarViewModel: CalendarViewModel by viewModels()

    /**
     * This method is called when the activity is first created.
     * Sets up the UI, initializes data passed through Intents, and configures SoundPool for audio playback.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the content view to the layout for this activity.
        setContentView(R.layout.melody_activity_melody)

        // Find the navigation controller to manage fragments within the activity.
        val navController = findNavController(R.id.nav_host_melody)

        // Retrieve any system ID and bells number passed via Intent
        val sysId = intent.getStringExtra("SYS_ID")
        val numBells = intent.getIntExtra("NUM_BELLS", 0)

        // If sysId and numBells are valid, assign them to the ViewModels.
        if (sysId != null && numBells > 0) {
            melodyViewModel.sysId = sysId
            melodyViewModel.nBells = numBells
            calendarViewModel.sysId = sysId
        }

        // SoundPool initialization
        melodyViewModel.soundPool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            SoundPool.Builder()
                .setMaxStreams(6)
                .setAudioAttributes(audioAttributes)
                .build()
        } else {
            SoundPool(6, AudioManager.STREAM_MUSIC, 0)
        }

        // Load the sound files into the SoundPool and map them to note names.
        melodyViewModel.soundMap["A"] = melodyViewModel.soundPool.load(this, R.raw.a4, 1)
        melodyViewModel.soundMap["B"] = melodyViewModel.soundPool.load(this, R.raw.b4, 1)
        melodyViewModel.soundMap["C"] = melodyViewModel.soundPool.load(this, R.raw.c4, 1)
        melodyViewModel.soundMap["D"] = melodyViewModel.soundPool.load(this, R.raw.d4, 1)
        melodyViewModel.soundMap["E"] = melodyViewModel.soundPool.load(this, R.raw.e4, 1)
        melodyViewModel.soundMap["F"] = melodyViewModel.soundPool.load(this, R.raw.f4, 1)
        melodyViewModel.soundMap["G"] = melodyViewModel.soundPool.load(this, R.raw.g4, 1)
    }
}
