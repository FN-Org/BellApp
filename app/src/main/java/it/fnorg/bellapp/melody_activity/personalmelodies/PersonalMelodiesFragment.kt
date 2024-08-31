package it.fnorg.bellapp.melody_activity.personalmelodies

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import it.fnorg.bellapp.R
import it.fnorg.bellapp.databinding.MelodyFragmentPersonalMelodiesBinding
import it.fnorg.bellapp.main_activity.MainActivity
import it.fnorg.bellapp.main_activity.home.HomeListAdapter
import it.fnorg.bellapp.melody_activity.MelodyViewModel

/**
 * A simple [Fragment] subclass.
 * Use the [PersonalMelodiesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PersonalMelodiesFragment : Fragment() {

    private val viewModel: MelodyViewModel by activityViewModels()

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

        viewModel.fetchMelodies()

        binding.backArrow.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            requireContext().startActivity(intent)
        }

        binding.rvMelody.layoutManager = LinearLayoutManager(requireContext())
        viewModel.melodyList.observe(viewLifecycleOwner , Observer { melodies ->
            if (melodies.isEmpty()) {
                binding.noMelodiesTV.visibility = View.VISIBLE
                binding.rvMelody.visibility = View.GONE
            } else {
                binding.noMelodiesTV.visibility = View.GONE
                binding.rvMelody.visibility = View.VISIBLE
                binding.rvMelody.adapter = MelodyAdapter(requireContext(), melodies, viewModel)
            }
        })

        binding.createMelodyButton.setOnClickListener {
            navController.navigate(R.id.action_personalMelodiesFragment_to_recordMelodyFragment)
        }
    }

    override fun onResume() {
        super.onResume()

        // Refresh system data when fragment resumes
        viewModel.fetchMelodies()
        viewModel.melodyList.observe(viewLifecycleOwner , Observer { melodies ->
            if (melodies.isEmpty()) {
                binding.noMelodiesTV.visibility = View.VISIBLE
                binding.rvMelody.visibility = View.GONE
            } else {
                binding.noMelodiesTV.visibility = View.GONE
                binding.rvMelody.visibility = View.VISIBLE
                binding.rvMelody.adapter = MelodyAdapter(requireContext(), melodies, viewModel)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.stopPlayback()
    }
}