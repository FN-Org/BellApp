package it.fnorg.bellapp.melody_activity.personalmelodies

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import it.fnorg.bellapp.R
import it.fnorg.bellapp.calendar_activity.CalendarViewModel
import it.fnorg.bellapp.databinding.MelodyFragmentPersonalMelodiesBinding
import it.fnorg.bellapp.main_activity.MainActivity
import it.fnorg.bellapp.melody_activity.MelodyViewModel

/**
 * This fragment displays a list of personal melodies and allows the user
 * to navigate to other screens such as recording a new melody or returning to the main activity.
 */
class PersonalMelodiesFragment : Fragment() {

    // Shared ViewModel instances between the fragment and its activity
    private val melodyViewModel: MelodyViewModel by activityViewModels()
    private val calendarViewModel : CalendarViewModel by activityViewModels()

    private lateinit var binding: MelodyFragmentPersonalMelodiesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MelodyFragmentPersonalMelodiesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()

        // Set click listener on the back arrow to return to the main activity
        binding.backArrow.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            requireContext().startActivity(intent)
        }

        // Set up RecyclerView with a linear layout manager
        binding.rvMelody.layoutManager = LinearLayoutManager(requireContext())

        // Observe the list of melodies from the ViewModel
        melodyViewModel.melodyList.observe(viewLifecycleOwner , Observer { melodies ->
            if (melodies.isEmpty()) {
                binding.noMelodiesTV.visibility = View.VISIBLE
                binding.rvMelody.visibility = View.GONE
            } else {
                binding.noMelodiesTV.visibility = View.GONE
                binding.rvMelody.visibility = View.VISIBLE

                // Set the adapter with the melody list
                binding.rvMelody.adapter =
                    MelodyAdapter(
                        requireContext(),
                        melodies,
                        melodyViewModel,
                        calendarViewModel,
                        viewLifecycleOwner)
            }
        })

        // Set click listener on the button to create a new melody
        binding.createMelodyButton.setOnClickListener {
            navController.navigate(R.id.action_personalMelodiesFragment_to_recordMelodyFragment)
        }
    }

    override fun onResume() {
        super.onResume()

        // Fetch system melodies and sync status when the fragment resumes
        melodyViewModel.fetchMelodies() // Fetch system melodies from firebase storage
        calendarViewModel.fetchMelodiesData() // Fetch system melodies from firebase firestore

        // Check the sync status of the system
        melodyViewModel.getSystemSync { result ->
            when (result) {
                false -> {
                    binding.syncMessage2.text = requireContext().getString(R.string.not_sync)
                    binding.syncMessage2.setTextColor(requireContext().getColor(R.color.warning))
                }
                true -> {
                    binding.syncMessage2.text = requireContext().getString(R.string.sync)
                    binding.syncMessage2.setTextColor(requireContext().getColor(R.color.white))
                }
                else -> {
                    Log.w("PersonalMelodiesFragment", "Sync is now null")
                }
            }
        }

        // Re-observe the melody list to update the UI
        melodyViewModel.melodyList.observe(viewLifecycleOwner , Observer { melodies ->
            if (melodies.isEmpty()) {
                binding.noMelodiesTV.visibility = View.VISIBLE
                binding.rvMelody.visibility = View.GONE
            } else {
                binding.noMelodiesTV.visibility = View.GONE
                binding.rvMelody.visibility = View.VISIBLE
                binding.rvMelody.adapter =
                    MelodyAdapter(
                        requireContext(),
                        melodies,
                        melodyViewModel,
                        calendarViewModel,
                        viewLifecycleOwner)
            }
        })
    }

    // Called when the fragment's view is destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        melodyViewModel.stopPlayback()
    }
}