package it.fnorg.bellapp.main_activity.home

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import it.fnorg.bellapp.databinding.MainHomeListItemBinding

class HomeListAdapter (var mContext: Context, var sysList: List<System>) : RecyclerView.Adapter<HomeListAdapter.SysHolder>() {

    inner class SysHolder(val view: MainHomeListItemBinding) : RecyclerView.ViewHolder(view.root)

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
        val sys = sysList.get(position)
        val view = holder.view
        if (sys.name != "") view.sysTitle.text = sys.name;
        else view.sysTitle.text = sys.id
        //view.calendarButton.setOnClickListener(OpenCalendarView(sys.id))
        // view.gradeItem.text = subject.grade.toString()
        // view.creditItem.text = subject.credits.toString()
    }
}