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

    private val melodyViewModel: MelodyViewModel by viewModels()
    private val calendarViewModel: CalendarViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.melody_activity_melody)

        val navController = findNavController(R.id.nav_host_melody)

        // Retrieve any system ID and bells number passed via Intent
        val sysId = intent.getStringExtra("SYS_ID")
        val numBells = intent.getIntExtra("NUM_BELLS", 0)

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

        // Notes sound
        melodyViewModel.soundMap["A"] = melodyViewModel.soundPool.load(this, R.raw.a4, 1)
        melodyViewModel.soundMap["B"] = melodyViewModel.soundPool.load(this, R.raw.b4, 1)
        melodyViewModel.soundMap["C"] = melodyViewModel.soundPool.load(this, R.raw.c4, 1)
        melodyViewModel.soundMap["D"] = melodyViewModel.soundPool.load(this, R.raw.d4, 1)
        melodyViewModel.soundMap["E"] = melodyViewModel.soundPool.load(this, R.raw.e4, 1)
        melodyViewModel.soundMap["F"] = melodyViewModel.soundPool.load(this, R.raw.f4, 1)
        melodyViewModel.soundMap["G"] = melodyViewModel.soundPool.load(this, R.raw.g4, 1)
    }
}
