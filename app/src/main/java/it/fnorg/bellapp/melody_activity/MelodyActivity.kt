package it.fnorg.bellapp.melody_activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import it.fnorg.bellapp.R

class MelodyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.melody_activity_melody)

        val navController = findNavController(R.id.nav_host_melody)
    }
}
