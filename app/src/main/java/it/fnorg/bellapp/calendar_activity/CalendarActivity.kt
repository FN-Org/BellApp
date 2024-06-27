package it.fnorg.bellapp.calendar_activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import it.fnorg.bellapp.R

class CalendarActivity : AppCompatActivity() {

    private val viewModel: CalendarActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calendar_activity_calendar)

        val sysId = intent.getStringExtra("id")

        val navController = findNavController(R.id.nav_host_calendar)

        viewModel.fetchEventsData(sysId.toString())
        viewModel.fetchMelodiesData(sysId.toString())
    }
}
