package it.fnorg.bellapp.main_activity.settings


import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.switchmaterial.SwitchMaterial
import androidx.constraintlayout.widget.Group
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import it.fnorg.bellapp.R
import it.fnorg.bellapp.main_activity.MainViewModel
import it.fnorg.bellapp.main_activity.ReminderReceiver

class SettingsFragment : Fragment() {


    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
            } else {
                // Explain to the user that the feature is unavailable because the
                // feature requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
            }
        }


    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent

    companion object {
        fun newInstance() = SettingsFragment()
    }

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(R.layout.main_fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val reminderSwitch : SwitchMaterial = view.findViewById(R.id.reminder_switch)

        alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        reminderSwitch.setOnCheckedChangeListener { _, isChecked ->
            val timeGroup : Group = view.findViewById(R.id.time_group)
            if (isChecked) {
                if (checkNotifyPermission(view)){
                    timeGroup.visibility = View.VISIBLE
                }
                else reminderSwitch.isEnabled = false
            } else {
                cancelAlarm(true)
                timeGroup.visibility = View.GONE
            }
        }

        val timeEditText : EditText = view.findViewById(R.id.reminderEditTextTime)

        timeEditText.setOnClickListener {
            cancelAlarm(false)
            val c = Calendar.getInstance()

            val hour = c.get(Calendar.HOUR_OF_DAY)
            val minute = c.get(Calendar.MINUTE)
            val timePickerDialog = TimePickerDialog(
                requireContext(),
                { view, hourOfDay, minute ->
                    val formattedTime = String.format("%02d:%02d", hourOfDay, minute)
                    timeEditText.setText(formattedTime)
                    setDailyAlarm(hourOfDay,minute)

                },
                hour,
                minute,
                false
            )

            timePickerDialog.show()
        }


    }

    private fun setDailyAlarm(hour: Int, minute: Int) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)

            // Se l'orario prefissato è già passato per oggi, allora imposta per domani
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        val intent = Intent(requireContext(), ReminderReceiver::class.java)
        Log.w("ReminderReceiver","created the intent")
        pendingIntent = PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE)
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
        Log.w("ReminderReceiver","setted the intent")
        Toast.makeText(requireContext(), "Daily Alarm Set", Toast.LENGTH_SHORT).show()
    }


    private fun cancelAlarm(show: Boolean) {
        if (::pendingIntent.isInitialized) {
            alarmManager.cancel(pendingIntent)

            if (show) Toast.makeText(requireContext(), "Alarm Cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkNotifyPermission(view: View): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
                    return true
                }
                else -> {

                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS)

                    return false

                }
            }
        }
        return false
    }

}