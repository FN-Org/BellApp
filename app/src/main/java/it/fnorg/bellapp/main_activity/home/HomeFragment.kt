package it.fnorg.bellapp.main_activity.home

import android.content.Intent
import androidx.lifecycle.Observer
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import it.fnorg.bellapp.CalendarActivity
import it.fnorg.bellapp.R
import it.fnorg.bellapp.databinding.MainFragmentHomeBinding

class HomeFragment : Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private val viewModel: HomeViewModel by viewModels()

    //binding connected to the specific layout of the fragment
    private lateinit var binding: MainFragmentHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout using data binding
        binding = MainFragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvHome.layoutManager = LinearLayoutManager(requireContext())
        viewModel.systems.observe(viewLifecycleOwner, Observer
        { systems ->
            // Update the RecyclerView
            val homeListAdapter = HomeListAdapter(requireContext(), systems)
            binding.rvHome.adapter = homeListAdapter
        })

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val uid = user.uid
            // Ora puoi utilizzare l'UID per le tue operazioni
            viewModel.fetchSysData(uid)
        } else {
            // Nessun utente attualmente autenticato
        }
    }
}