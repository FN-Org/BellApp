package it.fnorg.bellapp.calendar_activity.monthview

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import it.fnorg.bellapp.R
import it.fnorg.bellapp.calendar_activity.CalendarViewModel
import it.fnorg.bellapp.calendar_activity.Event
import it.fnorg.bellapp.calendar_activity.dateFormatter
import it.fnorg.bellapp.calendar_activity.eventDateTimeFormatter
import it.fnorg.bellapp.calendar_activity.timeFormatter
import it.fnorg.bellapp.calendar_activity.toLocalDateTime
import it.fnorg.bellapp.databinding.CalendarEventItemViewBinding
import it.fnorg.bellapp.isInternetAvailable

class EventListAdapter(
    private val context: Context,
    private val fragment: Fragment,
    val events: List<Event>
) :
    RecyclerView.Adapter<EventListAdapter.EventsViewHolder>() {

    private var viewModel: CalendarViewModel

    init {
        viewModel = ViewModelProvider(fragment).get(CalendarViewModel::class.java)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CalendarEventItemViewBinding.inflate(inflater, parent, false)
        return EventsViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: EventsViewHolder, position: Int) {
        viewHolder.bind(events[position])
    }

    override fun getItemCount(): Int = events.size

    inner class EventsViewHolder(val binding: CalendarEventItemViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(event: Event) {
            binding.itemEventDateText.apply {
                text = eventDateTimeFormatter.format(event.time.toLocalDateTime())
                setBackgroundColor(ContextCompat.getColor(context, viewModel.colorsList[event.color-1].color))
            }

            binding.itemMelodyNumberText.text = event.melodyNumber.toString()
            binding.itemMelodyNameText.text = event.melodyName

            // On click, goes to the add event fragment passing safe args
            itemView.setOnClickListener {
                if (!isInternetAvailable(context)) {
                    Toast.makeText(context, context.getString(R.string.connection_warning_1), Toast.LENGTH_SHORT).show()
                }
                else {
                    val id = event.id
                    val time = event.time.toLocalDateTime().toLocalTime().format(
                        timeFormatter
                    )
                    val date = event.time.toLocalDateTime().toLocalDate().format(
                        dateFormatter
                    )
                    val melody = event.melodyNumber
                    val color = event.color
                    val action = MonthViewFragmentDirections.actionMonthViewFragmentToAddEventFragment(id, time,date,melody,color)
                    binding.root.findNavController().navigate(action)
                }
            }
        }
    }
}