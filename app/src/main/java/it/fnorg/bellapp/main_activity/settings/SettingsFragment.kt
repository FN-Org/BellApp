package it.fnorg.bellapp.main_activity.settings

import android.Manifest
import android.app.AlarmManager
import android.app.AlertDialog
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
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.Group
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import it.fnorg.bellapp.R
import it.fnorg.bellapp.databinding.MainFragmentSettingsBinding
import it.fnorg.bellapp.main_activity.MainViewModel
import it.fnorg.bellapp.main_activity.ReminderReceiver
import it.fnorg.bellapp.openLink
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Fragment that allows users to manage application settings, including reminders, profile images,
 * and external links for contacts.
 */
class SettingsFragment : Fragment() {

    // Launcher for requesting notification permissions.
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        val context = requireContext()
        if (isGranted) {
            // Permission granted
            setPermissionRequested(context, true)
        } else {
            // Permission denied, show a message to enable notifications in settings
            Toast.makeText(context, R.string.notification_denied, Toast.LENGTH_LONG).show()
        }
    }

    // AlarmManager instance for setting and cancelling alarms.
    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent

    private val REMINDER_SET = booleanPreferencesKey("reminder_set")
    private val REMINDER_TIME_SET = stringPreferencesKey("reminder_time_set")

    companion object {
        fun newInstance() = SettingsFragment()
    }

    private val viewModel: MainViewModel by activityViewModels()

    // ViewBinding instance
    private lateinit var binding: MainFragmentSettingsBinding

    // Registers a photo picker activity launcher in single-select mode.
    val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle(R.string.change_image)
            // Set up the buttons
            builder.setPositiveButton(requireContext().getString(R.string.yes).uppercase()) { dialog, which ->
                Log.d("PhotoPicker", "Selected URI: $uri")
                Toast.makeText(requireContext(), requireContext().getString(R.string.image_uploading), Toast.LENGTH_SHORT).show()
                viewModel.uploadImageToFirebase(requireContext(), uri)
            }
            builder.setNegativeButton(requireContext().getString(R.string.no).uppercase()) { dialog, which ->
                dialog.cancel()
            }
            builder.show()
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout using ViewBinding
        binding = MainFragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize AlarmManager
        alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Observes userImage changes and updates ImageView using Glide
        viewModel.userImage.observe(viewLifecycleOwner) { userImage ->
            Log.w("Image", "Uri. " + userImage)
            Glide.with(this)
                .load(userImage)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.profileIv)
        }

        // Handle reminder switch state changes
        binding.reminderSwitch.setOnCheckedChangeListener { _, isChecked ->
            val timeGroup: Group = binding.timeGroup
            if (isChecked) {
                if (checkNotifyPermission(binding.root)) {
                    timeGroup.visibility = View.VISIBLE
                } else {
                    binding.reminderSwitch.isChecked = false
                }
            } else {
                cancelAlarm(true)
                timeGroup.visibility = View.GONE
            }

            viewLifecycleOwner.lifecycleScope.launch { setReminderPreference(binding.reminderSwitch.isChecked) }
        }

        // Handle click on time EditText to set reminder time
        binding.reminderEditTextTime.setOnClickListener {
            cancelAlarm(false)
            val c = Calendar.getInstance()
            var hour = c.get(Calendar.HOUR_OF_DAY)
            var minute = c.get(Calendar.MINUTE)

            if (binding.reminderEditTextTime.text.isNotBlank()) {
                val parts = binding.reminderEditTextTime.text.toString().split(":")
                hour = parts[0].toInt()
                minute = parts[1].toInt()
            }


            val timePickerDialog = TimePickerDialog(
                requireContext(),
                { view, hourOfDay, minute ->
                    val formattedTime = String.format("%02d:%02d", hourOfDay, minute)
                    binding.reminderEditTextTime.setText(formattedTime)
                    viewLifecycleOwner.lifecycleScope.launch { setReminderTimePreference(formattedTime) }
                    setDailyAlarm(hourOfDay, minute)
                },
                hour,
                minute,
                true
            )

            timePickerDialog.show()
        }

        val reminderSetFlow: Flow<Boolean> = requireContext().dataStore.data.map { settings ->
            settings[REMINDER_SET] == true
        }

        val reminderSetTimeFlow: Flow<String?> = requireContext().dataStore.data.map { settings ->
            settings[REMINDER_TIME_SET]
        }

        viewLifecycleOwner.lifecycleScope.launch {
            if (reminderSetFlow.first()) {
                binding.reminderSwitch.isChecked = true
                val reminderSetTime = reminderSetTimeFlow.first()
                if (reminderSetTime != null)
                    binding.reminderEditTextTime.setText(reminderSetTime)
            }
        }

        // Set click listener for image picker button
        binding.imagePicker.setOnClickListener {
            // Launch the photo picker and let the user choose only images.
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        // Set click listeners for external links (GitHub profiles)
        binding.githubFede.setOnClickListener {
            openLink(requireContext(), "https://github.com/fedeg202")
        }
        binding.githubNicco.setOnClickListener {
            openLink(requireContext(), "https://github.com/nicolotrebino")
        }
        binding.githubFnorg.setOnClickListener {
            openLink(requireContext(), "https://github.com/FN-Org")
        }
    }

    /**
     * Sets a daily repeating alarm at the specified hour and minute.
     *
     * @param hour Hour of the day to set the alarm.
     * @param minute Minute of the hour to set the alarm.
     */
    private fun setDailyAlarm(hour: Int, minute: Int) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)

            // If the specified time has already passed for today, set it for tomorrow
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

    /**
     * Cancels the currently set alarm.
     *
     * @param showToast Flag to indicate whether to show a toast message for cancellation.
     */
    private fun cancelAlarm(show: Boolean) {
        if (::pendingIntent.isInitialized) {
            alarmManager.cancel(pendingIntent)

            if (show) Toast.makeText(requireContext(), "Alarm Cancelled", Toast.LENGTH_SHORT).show()
            Log.w("ReminderReceiver","Deleted the intent")
        }
    }

    /**
     * Checks if the notification permission is granted.
     *
     * @param view View instance to handle permission rationale.
     * @return True if notification permission is granted, false otherwise.
     */
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
                    Toast.makeText(context, "Please enable notifications in settings", Toast.LENGTH_LONG).show()
                    return false
                }
                else -> {
                    if (!wasPermissionRequested(context)) {
                        // If the permission has not been requested before, request it
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        // If the permission has been requested and denied before, display a message to go to settings
                        Toast.makeText(context, "Please enable notifications in settings", Toast.LENGTH_LONG).show()
                    }
                    return false
                }
            }
        }
        return false
    }

    /**
     * Stores the state of whether the notification permission has been requested.
     *
     * @param context Context to access shared preferences.
     * @param value Boolean value indicating if the permission has been requested.
     */
    private fun setPermissionRequested(context: Context, value: Boolean) {
        val sharedPref = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putBoolean("notification_permission_requested", value)
            apply()
        }
    }

    /**
     * Checks if the notification permission has been requested before.
     *
     * @param context Context to access shared preferences.
     * @return Boolean value indicating if the permission has been requested before.
     */
    private fun wasPermissionRequested(context: Context): Boolean {
        val sharedPref = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE) ?: return false
        return sharedPref.getBoolean("notification_permission_requested", false)
    }

    /**
     * Stores the reminder switch state in the DataStore.
     *
     * @param value Boolean value indicating if the reminder is set.
     */
    private suspend fun setReminderPreference(value: Boolean) {
        requireContext().dataStore.edit { settings ->
            settings[REMINDER_SET] = value
        }
    }

    /**
     * Stores the reminder time in the DataStore.
     *
     * @param value String value representing the reminder time.
     */
    private suspend fun setReminderTimePreference(value: String) {
        requireContext().dataStore.edit { settings ->
            settings[REMINDER_TIME_SET] = value
        }
    }
}