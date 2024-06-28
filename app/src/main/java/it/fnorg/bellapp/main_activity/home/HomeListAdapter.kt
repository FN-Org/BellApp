package it.fnorg.bellapp.main_activity.home

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import it.fnorg.bellapp.R
import it.fnorg.bellapp.calendar_activity.CalendarActivity
import it.fnorg.bellapp.databinding.MainHomeListItemBinding
import it.fnorg.bellapp.main_activity.MainActivity
import it.fnorg.bellapp.main_activity.System

class HomeListAdapter (var mContext: Context, var sysList: List<System>) : RecyclerView.Adapter<HomeListAdapter.SysHolder>() {

    inner class SysHolder(val binding: MainHomeListItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SysHolder {
        val binding = MainHomeListItemBinding.inflate(LayoutInflater.from(mContext),
            parent, false)
        return SysHolder(binding)
    }

    override fun getItemCount(): Int {
        return sysList.size
    }

    override fun onBindViewHolder(holder: SysHolder, position: Int)
    {
        val sys = sysList[position]
        val binding = holder.binding

        if (sys.name.isNotBlank()) {
            binding.sysTitle.text = sys.name
        } else {
            binding.sysTitle.text = sys.id
        }

        binding.calendarButton.setOnClickListener {
            val intent = Intent(mContext, CalendarActivity::class.java).apply {
                putExtra("id", sys.id)
            }
            mContext.startActivity(intent)
        }

        binding.changeNameButton.setOnClickListener{
            showChangeNameInputDialog(sys.id)
        }
    }

    private fun showChangeNameInputDialog(sysId: String) {
        val builder = AlertDialog.Builder(mContext)
        builder.setTitle("Enter new name")

        // Set up the input
        val input = EditText(mContext)
        input.hint = "Enter name"
        builder.setView(input)

        // Set up the buttons
        builder.setPositiveButton("Save") { dialog, which ->
            val newName = input.text.toString()
            if (newName.isNotBlank()) {
                Log.w("ChangeName", newName)
                val viewModel = (mContext as MainActivity).viewModel
                viewModel.changeSysName(sysId, newName)
                viewModel.fetchSysData()
                Toast.makeText(mContext, R.string.name_changed, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(mContext, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.cancel()
        }

        builder.show()
    }
}