package it.fnorg.bellapp.main_activity.home

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import it.fnorg.bellapp.CalendarActivity
import it.fnorg.bellapp.databinding.MainHomeListItemBinding

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
        //view.calendarButton.setOnClickListener(OpenCalendarView(sys.id))
        // view.gradeItem.text = subject.grade.toString()
        // view.creditItem.text = subject.credits.toString()

        binding.calendarButton.setOnClickListener {
            val intent = Intent(mContext, CalendarActivity::class.java)
            mContext.startActivity(intent)
        }
    }
}