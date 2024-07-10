package it.fnorg.bellapp.calendar_activity.addevent

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
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
import it.fnorg.bellapp.calendar_activity.CalendarActivityViewModel
import it.fnorg.bellapp.calendar_activity.Event
import it.fnorg.bellapp.calendar_activity.Melody
import it.fnorg.bellapp.isInternetAvailable
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * A simple [Fragment] subclass.
 * Use the [AddEventFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddEventFragment : Fragment() {

    val viewModel: CalendarActivityViewModel by activityViewModels()

    // Safe args
    val args: AddEventFragmentArgs by navArgs()

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
            val adapter1 = MelodyAdapter(
                requireContext(),
                melodies
            )
            adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerMelodies.adapter = adapter1

            // Safe args
            if (args.eventId != "default") {
                fragmentTitle.text = requireContext().getString(R.string.modify_event)

                timeTextView.setText(args.eventTime)
                dateTextView.setText(args.eventDate)

                // Trova l'indice della melodia da selezionare
                val selectedMelodyIndex = melodies.indexOfFirst { it.number == args.eventMelody }
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
                fragmentTitle.text = requireContext().getString(R.string.add_event)
            }

            // Notifica all'adapter che i dati sono cambiati
            adapter1.notifyDataSetChanged()
        }

        spinnerMelodies.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Esegui le azioni desiderate con l'opzione selezionata
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Non esegue alcuna azione quando non viene selezionata alcuna opzione
            }
        }

        // Spinner for colors
        val adapter2 = ColorAdapter(requireContext(), viewModel.colorsList)
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerColors.adapter = adapter2

        spinnerColors.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Esegui le azioni desiderate con l'opzione selezionata
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Non esegue alcuna azione quando non viene selezionata alcuna opzione
            }
        }

        // TimerPickerDialog
        timeTextView.setOnClickListener {
            val c = Calendar.getInstance()

            val hour = c.get(Calendar.HOUR_OF_DAY)
            val minute = c.get(Calendar.MINUTE)
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

            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

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

                // Converte LocalDateTime in istante di tempo in millisecondi UNIX
                val epochMillis = dateTime.toInstant(ZoneOffset.ofHours(2)).toEpochMilli()

                // Creazione di un oggetto Timestamp per Firebase Firestore
                val timestamp = Timestamp(epochMillis / 1000, (epochMillis % 1000).toInt())

                val selectedMelody = spinnerMelodies.selectedItem as Melody
                val melodyNumber = selectedMelody.number
                val melodyName = selectedMelody.name

                val color = spinnerColors.selectedItemPosition + 1

                val event = Event(
                    id = "", // L'ID verrà generato automaticamente da Firestore
                    time = timestamp,
                    melodyName = melodyName,
                    melodyNumber = melodyNumber,
                    color = color
                )

                viewModel.saveEvent(requireContext(), event, args.eventId)

                view.findNavController().navigate(R.id.action_addEventFragment_to_monthViewFragment)
            }
        }
    }
}