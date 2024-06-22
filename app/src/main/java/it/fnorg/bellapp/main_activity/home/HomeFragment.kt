package it.fnorg.bellapp.main_activity.home

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat
import androidx.recyclerview.widget.LinearLayoutManager
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
        return inflater.inflate(R.layout.main_fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rv_home.layoutManager = LinearLayoutManager(ContentProviderCompat.requireContext())
        viewModel.subjects.observe(viewLifecycleOwner, Observer
        { sys ->
            // Update the RecyclerView
            val gradeAdapter = HomeListAdapter(ContentProviderCompat.requireContext(), subjects)
            binding.rv.adapter = gradeAdapter
        })
    }
}