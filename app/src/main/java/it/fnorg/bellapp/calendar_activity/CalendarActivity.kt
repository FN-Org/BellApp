package it.fnorg.bellapp.calendar_activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.google.firebase.auth.FirebaseAuth
import it.fnorg.bellapp.R
import it.fnorg.bellapp.login_activity.LogInActivity

class CalendarActivity : AppCompatActivity() {

    private val viewModel: CalendarActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calendar_activity_calendar)

        if (FirebaseAuth.getInstance().currentUser == null) {
            val intent = Intent(this, LogInActivity::class.java)
            startActivity(intent)
        }
        val sysId = intent.getStringExtra("id")

        if (sysId != null) {
            viewModel.sysId = sysId

            Log.w("BellApp-CalendarActViewModel", "Obtained sysId: -$sysId-")
        }

        val navController = findNavController(R.id.nav_host_calendar)

        viewModel.fetchEventsData()
        viewModel.fetchMelodiesData()
    }
}
