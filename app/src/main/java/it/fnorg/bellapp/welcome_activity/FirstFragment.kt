package it.fnorg.bellapp.welcome_activity

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.findNavController
import it.fnorg.bellapp.login_activity.LogInActivity
import it.fnorg.bellapp.R

/**
 * A Fragment representing the first screen in the welcome sequence.
 */
class FirstFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.welcome_fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find the buttons in the view
        val nextButton : Button = view.findViewById(R.id.nButton1)
        val skipButton : Button = view.findViewById(R.id.sButton1)

        // Set click listener for the "Next" button
        nextButton.setOnClickListener {
            view.findNavController().navigate(R.id.action_firstFragment_to_secondFragment)
        }

        // Set click listener for the "Skip" button
        skipButton.setOnClickListener {
            val intent = Intent(requireContext(), LogInActivity::class.java)
            startActivity(intent)
        }
    }
}