package it.fnorg.bellapp.main_activity.addsys

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import it.fnorg.bellapp.R

class AddSysFragment : Fragment() {

    companion object {
        fun newInstance() = AddSysFragment()
    }

    private val viewModel: AddSysViewModel by viewModels()

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
}