package it.fnorg.bellapp.main_activity.home

import android.content.res.ColorStateList
import androidx.lifecycle.Observer
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import it.fnorg.bellapp.R
import it.fnorg.bellapp.databinding.MainFragmentHomeBinding
import it.fnorg.bellapp.main_activity.MainViewModel

/**
 * Fragment representing the home screen of the application.
 */
class HomeFragment : Fragment() {

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
        viewModel.systems.observe(viewLifecycleOwner, Observer { systems ->
            if (systems.isNullOrEmpty()) {
                binding.homeMessage.visibility = View.VISIBLE
                binding.rvHome.visibility = View.GONE
            } else {
                binding.homeMessage.visibility = View.GONE
                binding.rvHome.visibility = View.VISIBLE
                val homeListAdapter = HomeListAdapter(requireContext(), systems, viewModel)
                binding.rvHome.adapter = homeListAdapter
            }
        })

        val searchBar = binding.searchView
        val fab = binding.homeSearchButton

        // Set a click listener for the floating action button
        // to open the search bar and filter systems
        fab.setOnClickListener {
            if (searchBar.visibility == View.GONE) {
                searchBar.visibility = View.VISIBLE
                searchBar.setIconifiedByDefault(false)
                searchBar.isIconified = true
                searchBar.isIconified = false
                searchBar.requestFocusFromTouch()
                fab.setImageResource(R.drawable.ic_close)
                val blackColor = ContextCompat.getColor(requireContext(), R.color.black)
                ImageViewCompat.setImageTintList(fab, ColorStateList.valueOf(blackColor))
            }
            else {
                searchBar.visibility = View.GONE
                fab.setImageResource(R.drawable.ic_search)
                searchBar.setQuery("", false)
                searchBar.clearFocus()
            }
        }

        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                (binding.rvHome.adapter as? HomeListAdapter)?.filter(newText.orEmpty())
                return true
            }
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