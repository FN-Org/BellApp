package it.fnorg.bellapp.melody_activity

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import it.fnorg.bellapp.R
import it.fnorg.bellapp.databinding.MainFragmentHomeBinding
import it.fnorg.bellapp.databinding.MelodyFragmentPersonalMelodiesBinding
import it.fnorg.bellapp.databinding.MelodyFragmentRecordMelodyBinding
import it.fnorg.bellapp.main_activity.MainActivity

/**
 * A simple [Fragment] subclass.
 * Use the [PersonalMelodiesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PersonalMelodiesFragment : Fragment() {

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

        binding.backArrow.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            requireContext().startActivity(intent)
        }

        binding.createMelodyButton.setOnClickListener {
            navController.navigate(R.id.action_personalMelodiesFragment_to_recordMelodyFragment)
        }
    }
}