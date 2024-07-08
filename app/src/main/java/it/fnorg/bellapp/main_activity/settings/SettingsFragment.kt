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
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import it.fnorg.bellapp.R
import it.fnorg.bellapp.main_activity.MainViewModel
import it.fnorg.bellapp.main_activity.ReminderReceiver
import it.fnorg.bellapp.main_activity.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {


    val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        val context = requireContext()
        if (isGranted) {
            // Il permesso è stato concesso
            setPermissionRequested(context, true)
        } else {
            // Il permesso è stato negato, mostra un messaggio per andare nelle impostazioni
            Toast.makeText(context, "Please enable notifications in settings", Toast.LENGTH_LONG).show()
        }
    }


    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent

    private val REMINDER_SET = booleanPreferencesKey("reminder_set")
    private val REMINDER_TIME_SET = stringPreferencesKey("reminder_time_set")

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

        val reminderSwitch: SwitchMaterial = view.findViewById(R.id.reminder_switch)

        alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        reminderSwitch.setOnCheckedChangeListener { _, isChecked ->
            val timeGroup: Group = view.findViewById(R.id.time_group)
            if (isChecked) {
                if (checkNotifyPermission(view)) {
                    timeGroup.visibility = View.VISIBLE
                } else {
                    reminderSwitch.isChecked = false // Disabilita lo switch se i permessi non sono concessi
                }
            } else {
                cancelAlarm(true)
                timeGroup.visibility = View.GONE
            }

            viewLifecycleOwner.lifecycleScope.launch {setReminderPreference(reminderSwitch.isChecked)}
        }

        val timeEditText: EditText = view.findViewById(R.id.reminderEditTextTime)

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
                    viewLifecycleOwner.lifecycleScope.launch {setReminderTimePreference(formattedTime)}
                    setDailyAlarm(hourOfDay, minute)
                },
                hour,
                minute,
                true
            )

            timePickerDialog.show()
        }

        var reminderSetFlow: Flow<Boolean> = requireContext().dataStore.data.map { settings ->
            settings[REMINDER_SET] == true
        }

        var reminderSetTimeFlow : Flow<String?> = requireContext().dataStore.data.map { settings ->
            settings[REMINDER_TIME_SET]
        }

        viewLifecycleOwner.lifecycleScope.launch{
            if (reminderSetFlow.first()) {
                reminderSwitch.isChecked = true
                val reminderSetTime = reminderSetTimeFlow.first()
                if (reminderSetTime != null)
                    timeEditText.setText(reminderSetTime)
            }
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
        val context = requireContext()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            when {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    return true
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Mostra un messaggio che spiega perché il permesso è necessario e chiede di andare nelle impostazioni
                    Toast.makeText(context, "Please enable notifications in settings", Toast.LENGTH_LONG).show()
                    return false
                }
                else -> {
                    if (!wasPermissionRequested(context)) {
                        // Chiede il permesso per la prima volta
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        setPermissionRequested(context, true)
                    } else {
                        // Se il permesso è stato richiesto e negato, mostra un messaggio per andare nelle impostazioni
                        Toast.makeText(context, "Please enable notifications in settings", Toast.LENGTH_LONG).show()
                    }
                    return false
                }
            }
        }
        return false
    }


    private fun setPermissionRequested(context: Context, value: Boolean) {
        val sharedPref = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putBoolean("notification_permission_requested", value)
            apply()
        }
    }

    private fun wasPermissionRequested(context: Context): Boolean {
        val sharedPref = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE) ?: return false
        return sharedPref.getBoolean("notification_permission_requested", false)
    }


    private suspend fun setReminderPreference(value: Boolean) {
        requireContext().dataStore.edit { settings ->
            settings[REMINDER_SET] = value
        }
    }

    private suspend fun setReminderTimePreference(value: String) {
        requireContext().dataStore.edit { settings ->
            settings[REMINDER_TIME_SET] = value
        }
    }

}