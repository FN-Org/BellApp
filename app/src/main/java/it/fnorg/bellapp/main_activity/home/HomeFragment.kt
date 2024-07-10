package it.fnorg.bellapp.main_activity.home

import androidx.lifecycle.Observer
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import it.fnorg.bellapp.R
import it.fnorg.bellapp.databinding.MainFragmentHomeBinding
import it.fnorg.bellapp.main_activity.MainViewModel

/**
 * Fragment representing the home screen of the application.
 */
class HomeFragment : Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private val viewModel: MainViewModel by activityViewModels()

    // Binding connected to the specific layout of the fragment
    private lateinit var binding: MainFragmentHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Handle back press to show toast and confirm exit
        requireActivity().onBackPressedDispatcher.addCallback(this){
            Toast.makeText(requireContext(),getString(R.string.back_before_exit_toast),Toast.LENGTH_SHORT).show()
            requireActivity().onBackPressedDispatcher.addCallback(requireActivity()){
                requireActivity().finishAffinity()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MainFragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvHome.layoutManager = LinearLayoutManager(requireContext())
        viewModel.systems.observe(viewLifecycleOwner, Observer
        { systems ->
            val homeListAdapter = HomeListAdapter(requireContext(), systems, viewModel)
            binding.rvHome.adapter = homeListAdapter
        })

        // Fetch system data when view is created or resumed
        viewModel.fetchSysHomeData()
    }

    override fun onResume() {
        super.onResume()

        // Refresh system data when fragment resumes
        viewModel.fetchSysHomeData()
    }
}