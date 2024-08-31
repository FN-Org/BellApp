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

class MelodyActivity : AppCompatActivity() {

    private val viewModel: MelodyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.melody_activity_melody)

        val navController = findNavController(R.id.nav_host_melody)

        // Retrieve any system ID and bells number passed via Intent
        val sysId = intent.getStringExtra("SYS_ID")
        val numBells = intent.getIntExtra("NUM_BELLS", 0)

        if (sysId != null && numBells > 0) {
            viewModel.sysId = sysId
            viewModel.nBells = numBells
        }

        // SoundPool initialization
        viewModel.soundPool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
        viewModel.soundMap["A"] = viewModel.soundPool.load(this, R.raw.a4, 1)
        viewModel.soundMap["B"] = viewModel.soundPool.load(this, R.raw.b4, 1)
        viewModel.soundMap["C"] = viewModel.soundPool.load(this, R.raw.c4, 1)
        viewModel.soundMap["D"] = viewModel.soundPool.load(this, R.raw.d4, 1)
        viewModel.soundMap["E"] = viewModel.soundPool.load(this, R.raw.e4, 1)
        viewModel.soundMap["F"] = viewModel.soundPool.load(this, R.raw.f4, 1)
        viewModel.soundMap["G"] = viewModel.soundPool.load(this, R.raw.g4, 1)
    }
}
