package it.fnorg.bellapp.main_activity.addsys

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.Group
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.firebase.messaging.FirebaseMessaging
import it.fnorg.bellapp.R
import it.fnorg.bellapp.databinding.MainFragmentAddSysBinding
import it.fnorg.bellapp.isInternetAvailable
import it.fnorg.bellapp.main_activity.MainViewModel
import it.fnorg.bellapp.updateFCMTokenToSystems

/**
 * Fragment for adding a new system.
 */
class AddSysFragment : Fragment() {

    companion object {
        fun newInstance() = AddSysFragment()
    }

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var binding: MainFragmentAddSysBinding // Declare binding variable

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout using view binding
        binding = MainFragmentAddSysBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize UI elements using binding
        val searchButton: Button = binding.searchButton
        val idEditTV: EditText = binding.sysIdEditText
        val foundTV: TextView = binding.foundTextView
        val sysDataGroup: Group = binding.sysDataFound
        val addSysButton: Button = binding.addSysButton
        val pinEditTv: EditText = binding.editTextTextSysPin

        val idTv: TextView = binding.idTV
        val nameTv: TextView = binding.nameTV
        val locationTv: TextView = binding.locationTV
        val numBellTv: TextView = binding.numBellTV
        val numMelodiesTv: TextView = binding.numMelodiesTV

        var sysPin = 10
        var sysId = ""
        var sysLocation = ""
        var sysName = ""

        viewModel.system.observe(viewLifecycleOwner, Observer { system ->
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
                } else if (!isInternetAvailable(requireContext())) {
                    Toast.makeText(requireContext(), requireContext().getString(R.string.connection_warning_2), Toast.LENGTH_SHORT).show()
                    foundTV.visibility = View.INVISIBLE
                    sysDataGroup.visibility = View.INVISIBLE
                } else {
                    viewModel.fetchSysData(enteredId) { success ->
                        if (success) {
                            foundTV.visibility = View.VISIBLE
                            foundTV.text = requireContext().getString(R.string.found)
                            sysDataGroup.visibility = View.VISIBLE
                        } else {
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

        addSysButton.setOnClickListener {
            if ((pinEditTv.text.toString().isNotBlank()
                        && sysId.isNotBlank() && sysLocation.isNotBlank() && sysName.isNotBlank()
                        && pinEditTv.text.toString().toInt() == sysPin)
                && isInternetAvailable(requireContext())) {

                viewModel.addSys(sysId, sysLocation, sysName) { result ->
                    if (result) {
                        val systemIds: MutableList<String> = mutableListOf()
                        systemIds.add(sysId)
                        updateFCMTokenToSystems(FirebaseMessaging.getInstance().token.toString(), systemIds)

                        Toast.makeText(requireContext(), R.string.successfully_add_sys, Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_nav_add_sys_to_nav_home2)
                    }
                    else {
                        Toast.makeText(requireContext(), R.string.sww_try_again, Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), R.string.sww_try_again, Toast.LENGTH_LONG).show()
            }
        }
    }
}
