package it.fnorg.bellapp.melody_activity

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
    }
}
