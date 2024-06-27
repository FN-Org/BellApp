package it.fnorg.bellapp.calendar_activity

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import it.fnorg.bellapp.R

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

        viewModel.fetchMelodiesData(viewModel.sysId)

        val spinnerMelodies: Spinner = view.findViewById(R.id.spinner_options_melodies)
        val spinnerColors: Spinner = view.findViewById(R.id.spinner_options_colors)
        val timeTextView: EditText = view.findViewById(R.id.editTextTime)
        val dateTextView: EditText = view.findViewById(R.id.editTextDate)
        val fragmentTitle: TextView = view.findViewById(R.id.fragment_title)
        val backArrow: ImageView = view.findViewById(R.id.eventBackArrow)

        backArrow.setOnClickListener {
            view.findNavController().navigate(R.id.action_addEventFragment_to_monthViewFragment)
        }

        // Melodies' observer & spinner for melodies
        viewModel.melodies.observe(viewLifecycleOwner) { melodies ->
            val adapter1 = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                melodies.map { it.name }
            )
            adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerMelodies.adapter = adapter1
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
                    timeTextView.setText("$hourOfDay:$minute")
                },
                hour,
                minute,
                false
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
                    dateTextView.setText(dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year)
                },
                year,
                month,
                day
            )

            datePickerDialog.show()
        }

        // Safe args
        if (args.eventTime != "default"){
            fragmentTitle.text = requireContext().getString(R.string.modify_event)

            timeTextView.setText(args.eventTime)
            dateTextView.setText(args.eventDate)

            val spinnerMelodies: Spinner = view.findViewById(R.id.spinner_options_melodies)
            spinnerMelodies.setSelection(args.eventMelody-1)

            val spinnerColors : Spinner = view.findViewById(R.id.spinner_options_colors)
            spinnerColors.setSelection(args.eventColor-1)
        }
        else fragmentTitle.text = requireContext().getString(R.string.add_event)

    }
}