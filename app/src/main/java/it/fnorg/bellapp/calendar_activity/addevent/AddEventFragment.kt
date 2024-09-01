package it.fnorg.bellapp.calendar_activity.addevent

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.Timestamp
import it.fnorg.bellapp.R
import it.fnorg.bellapp.calendar_activity.CalendarViewModel
import it.fnorg.bellapp.calendar_activity.Event
import it.fnorg.bellapp.calendar_activity.Melody
import it.fnorg.bellapp.isInternetAvailable
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Fragment for managing events in the calendar, including creation, deletion, and updates.
 */
class AddEventFragment : Fragment() {

    private val viewModel: CalendarViewModel by activityViewModels()

    // Safe args passed from MonthView Fragment
    private val args: AddEventFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.calendar_fragment_add_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val spinnerMelodies: Spinner = view.findViewById(R.id.spinner_options_melodies)
        val spinnerColors: Spinner = view.findViewById(R.id.spinner_options_colors)
        val timeTextView: EditText = view.findViewById(R.id.editTextTime)
        val dateTextView: EditText = view.findViewById(R.id.editTextDate)
        val fragmentTitle: TextView = view.findViewById(R.id.fragment_title)
        val backArrow: ImageView = view.findViewById(R.id.eventBackArrow)
        val saveButton: Button = view.findViewById(R.id.save_button)
        val deleteButton: Button = view.findViewById(R.id.delete_button)

        backArrow.setOnClickListener {
            view.findNavController().navigate(R.id.action_addEventFragment_to_monthViewFragment)
        }

        // deleteButton invisible and not clickable as default options
        deleteButton.visibility = View.GONE
        deleteButton.isEnabled = false

        // Melodies' observer & spinner for melodies
        viewModel.melodies.observe(viewLifecycleOwner) { melodies ->

            val sortedMelodies = melodies.sortedBy { it.number }

            val adapter1 = MelodyAdapter(
                requireContext(),
                sortedMelodies
            )
            adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerMelodies.adapter = adapter1

            // Safe args
            if (args.eventId != "default") {
                fragmentTitle.text = requireContext().getString(R.string.modify_event)

                timeTextView.setText(args.eventTime)
                dateTextView.setText(args.eventDate)

                // Find the selected melody index
                val selectedMelodyIndex = sortedMelodies.indexOfFirst { it.number == args.eventMelody }
                if (selectedMelodyIndex != -1) {
                    spinnerMelodies.setSelection(selectedMelodyIndex)
                }
                spinnerColors.setSelection(args.eventColor - 1)

                // defaultButton become visible and clickable in the Modify Event
                deleteButton.visibility = View.VISIBLE
                deleteButton.isEnabled = true
                deleteButton.setOnClickListener {
                    if (!isInternetAvailable(requireContext())) {
                        Toast.makeText(requireContext(), requireContext().getString(R.string.sww_connection), Toast.LENGTH_SHORT).show()
                        view.findNavController().navigate(R.id.action_addEventFragment_to_monthViewFragment)
                    } else {
                        viewModel.deleteEvent(args.eventId)
                        Toast.makeText(requireActivity(), R.string.deleted, Toast.LENGTH_SHORT).show()
                        view.findNavController().navigate(R.id.action_addEventFragment_to_monthViewFragment)
                    }
                }
            } else {
                if (args.eventDate != "default")
                    dateTextView.setText(args.eventDate)
                fragmentTitle.text = requireContext().getString(R.string.add_event)
            }
        }

        // Spinner for colors
        val adapter2 = ColorAdapter(requireContext(), viewModel.colorsList)
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerColors.adapter = adapter2

        // TimerPickerDialog
        timeTextView.setOnClickListener {
            val c = Calendar.getInstance()

            var hour = c.get(Calendar.HOUR_OF_DAY)
            var minute = c.get(Calendar.MINUTE)

            if (timeTextView.text.isNotBlank()) {
                val parts = timeTextView.text.toString().split(":")
                hour = parts[0].toInt()
                minute = parts[1].toInt()
            }

            val timePickerDialog = TimePickerDialog(
                requireContext(),
                { view, hourOfDay, minute ->
                    val formattedTime = String.format("%02d:%02d", hourOfDay, minute)
                    timeTextView.setText(formattedTime)
                },
                hour,
                minute,
                true
            )

            timePickerDialog.show()
        }

        // DatePickerDialog
        dateTextView.setOnClickListener {
            val c = Calendar.getInstance()

            var year = c.get(Calendar.YEAR)
            var month = c.get(Calendar.MONTH)
            var day = c.get(Calendar.DAY_OF_MONTH)

            if (dateTextView.text.isNotBlank()) {
                val parts = dateTextView.text.toString().split("-")
                day = parts[0].toInt()
                month = parts[1].toInt() - 1
                year = parts[2].toInt()
            }

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { view, year, monthOfYear, dayOfMonth ->
                    val formattedDate = String.format("%02d-%02d-%02d", dayOfMonth, monthOfYear+1, year)
                    dateTextView.setText(formattedDate)
                },
                year,
                month,
                day
            )

            datePickerDialog.show()
        }

        saveButton.setOnClickListener {
            // Update or create the event only if you are connected
            if (!isInternetAvailable(requireContext())) {
                Toast.makeText(requireContext(), requireContext().getString(R.string.sww_connection), Toast.LENGTH_SHORT).show()
                view.findNavController().navigate(R.id.action_addEventFragment_to_monthViewFragment)
            }
            else {
                val time = timeTextView.text.toString()
                val date = dateTextView.text.toString()

                if (time.isBlank()||date.isBlank()){
                    Toast.makeText(requireActivity(), R.string.incorrect_saved, Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                // LocalDateTime
                val dateTimeString = "$date $time"
                val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
                val dateTime = LocalDateTime.parse(dateTimeString.trim(), formatter)

                // Convert LocalDateTime in UNIX milliseconds
                val epochMillis = dateTime.toInstant(ZoneOffset.ofHours(2)).toEpochMilli()

                // Timestamp creation for Firestore DB
                val timestamp = Timestamp(epochMillis / 1000, (epochMillis % 1000).toInt())

                val selectedMelody = spinnerMelodies.selectedItem as Melody
                val melodyNumber = selectedMelody.number
                val melodyName = selectedMelody.name

                val color = spinnerColors.selectedItemPosition + 1

                val event = Event(
                    id = "", // Automatic ID will be created by Firestore
                    time = timestamp,
                    melodyName = melodyName,
                    melodyNumber = melodyNumber,
                    color = color
                )

                viewModel.saveEvent(event, args.eventId) { result ->
                    when(result){
                        1-> Toast.makeText(context,R.string.event_created, Toast.LENGTH_SHORT).show()
                        2-> Toast.makeText(context,R.string.event_updated, Toast.LENGTH_SHORT).show()
                        -1 -> Toast.makeText(context,R.string.sww_try_again, Toast.LENGTH_SHORT).show()
                        -2 -> {
                            val sww_try_again = context?.getString(R.string.sww_try_again)
                            val updating_oldEvent =
                                context?.getString(R.string.updating_oldEvent)?.lowercase()
                            Toast.makeText(context,"$sww_try_again,$updating_oldEvent", Toast.LENGTH_LONG).show()
                        }

                        else -> Toast.makeText(context,R.string.sww_try_again,Toast.LENGTH_SHORT).show()
                    }
                }

                view.findNavController().navigate(R.id.action_addEventFragment_to_monthViewFragment)
            }
        }
    }
}