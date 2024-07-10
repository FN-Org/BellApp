package it.fnorg.bellapp.main_activity.addsys

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.Group
import androidx.fragment.app.activityViewModels
import it.fnorg.bellapp.R
import it.fnorg.bellapp.databinding.MainFragmentAddSysBinding
import it.fnorg.bellapp.main_activity.MainViewModel
import androidx.lifecycle.Observer
import androidx.lifecycle.map
import androidx.navigation.fragment.findNavController
import it.fnorg.bellapp.checkConnection
import it.fnorg.bellapp.isInternetAvailable
import it.fnorg.bellapp.main_activity.System

class AddSysFragment : Fragment() {

    companion object {
        fun newInstance() = AddSysFragment()
    }

    private val viewModel: MainViewModel by activityViewModels()

    //binding connected to the specific layout of the fragment
    private lateinit var binding: MainFragmentAddSysBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment_add_sys, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val searchButton : Button = view.findViewById(R.id.search_button)
        val idEditTV : EditText = view.findViewById(R.id.sys_id_edit_text)
        val foundTV : TextView = view.findViewById(R.id.foundTextView)
        val sysDataGroup : Group = view.findViewById(R.id.sysDataFound)
        val addSysButton : Button = view.findViewById(R.id.add_sys_button)
        val pinEditTv : EditText = view.findViewById(R.id.editTextTextSysPin)

        val idTv : TextView = view.findViewById(R.id.id_TV)
        val nameTv :TextView = view.findViewById(R.id.name_TV)
        val locationTv : TextView = view.findViewById(R.id.location_TV)
        val numBellTv : TextView = view.findViewById(R.id.num_bell_TV)
        val numMelodiesTv : TextView = view.findViewById(R.id.num_melodies_TV)

        var sysPin = 10
        var sysId = ""
        var sysLocation = ""
        var sysName = ""

        viewModel.system.observe(viewLifecycleOwner, Observer
        { system ->
            numBellTv.text = system.nBells.toString()
            numMelodiesTv.text = system.nMelodies.toString()
            locationTv.text = system.location
            nameTv.text = system.name
            idTv.text = system.id

            sysPin = system.pin
            sysId = system.id
            sysName = system.name
            sysLocation = system.location
        })

        searchButton.setOnClickListener {
            val enteredId = idEditTV.text.toString().trim()
            if (enteredId.isNotBlank()) {
                if (viewModel.systems.value?.any { it.id == enteredId } == true) {
                    sysDataGroup.visibility = View.INVISIBLE
                    foundTV.visibility = View.VISIBLE
                    foundTV.text = buildString {
                        append(requireContext().getString(R.string.found))
                        append("\n")
                        append(requireContext().getString(R.string.already_linked))
                    }
                }
                else if (!isInternetAvailable(requireContext())) {
                    Toast.makeText(requireContext(), requireContext().getString(R.string.connection_warning_2), Toast.LENGTH_SHORT).show()
                    foundTV.visibility = View.INVISIBLE
                    sysDataGroup.visibility = View.INVISIBLE
                }
                else {
                    viewModel.fetchSysData(enteredId) { success ->
                        if (success) {
                            // Handle success
                            foundTV.visibility = View.VISIBLE
                            foundTV.text = requireContext().getString(R.string.found)
                            sysDataGroup.visibility = View.VISIBLE
                        } else {
                            // Handle failure
                            sysDataGroup.visibility = View.INVISIBLE
                            foundTV.visibility = View.VISIBLE
                            foundTV.text = buildString {
                                append(requireContext().getString(R.string.found))
                                append("\n")
                                append(requireContext().getString(R.string.none))
                            }
                        }
                    }
                }
            }
        }

        addSysButton.setOnClickListener{
            if ((pinEditTv.text.toString().isNotBlank()
                && sysId.isNotBlank() && sysLocation.isNotBlank() && sysName.isNotBlank()
                && pinEditTv.text.toString().toInt() == sysPin)
                && isInternetAvailable(requireContext()))
            {
                viewModel.addSys(sysId, sysLocation, sysName)
                Toast.makeText(requireContext(),R.string.successfully_add_sys,Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_nav_add_sys_to_nav_home2)
            }
            else Toast.makeText(requireContext(),R.string.sww_try_again,Toast.LENGTH_LONG).show()
        }
    }
}