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
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import it.fnorg.bellapp.R

data class Melody(
    val id: Int,
    val name: String
)

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AddEventFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddEventFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    //Safe args
    val args: AddEventFragmentArgs by navArgs()

    val colorsList = listOf(
        Color("Yellow", R.color.naples),
        Color("Red", R.color.lightred),
        Color("Green", R.color.jade)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.calendar_fragment_add_event, container, false)

        // Spinner for melodies selection
        val spinner1: Spinner = view.findViewById(R.id.spinner_options_melodies)

        val melodiesList = listOf(
            Melody(1, "Opzione 1"),
            Melody(2, "Opzione 2"),
            Melody(3, "Opzione 3"),
        )

        val adapter1 = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            melodiesList.map { it.name }
        )

        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner1.adapter = adapter1

        spinner1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Esegui le azioni desiderate con l'opzione selezionata
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Non esegue alcuna azione quando non viene selezionata alcuna opzione
            }
        }

        // Spinner for colors selection
        val spinner2: Spinner = view.findViewById(R.id.spinner_options_colors)



        val adapter2 = ColorAdapter(requireContext(), colorsList)
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner2.adapter = adapter2

        spinner2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Esegui le azioni desiderate con l'opzione selezionata
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Non esegue alcuna azione quando non viene selezionata alcuna opzione
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<ImageView>(R.id.eventBackArrow).setOnClickListener{
            view.findNavController().navigate(R.id.action_addEventFragment_to_monthViewFragment)
        }

        val timeTextView: EditText = view.findViewById(R.id.editTextTime)
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

        val dateTextView : EditText = view.findViewById(R.id.editTextDate)
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

        val fragmentTitle : TextView = view.findViewById(R.id.fragment_title)

        //Safe args
        if (args.eventTime != "default"){
            fragmentTitle.text = "Modify Event"

            timeTextView.setText(args.eventTime)
            dateTextView.setText(args.eventDate)

            val spinnerMelodies: Spinner = view.findViewById(R.id.spinner_options_melodies)
            spinnerMelodies.setSelection(args.eventMelody)

            val spinnerColors : Spinner = view.findViewById(R.id.spinner_options_colors)
            // TODO: colorsList.indexOfFirst { it.colorResId == args.eventColor } or something to
            // find the right index of the color in the spinner instead of pre-defined 0
            spinnerColors.setSelection(0)
        }
        else fragmentTitle.text = "Add Event"




    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AddEventFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddEventFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}