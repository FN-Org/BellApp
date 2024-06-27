package it.fnorg.bellapp.calendar_activity

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import it.fnorg.bellapp.databinding.CalendarEventItemViewBinding

class EventListAdapter(
    context: Context,
    val events: List<Event>
) :
    RecyclerView.Adapter<EventListAdapter.EventsViewHolder>() {

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
                text = eventDateTimeFormatter.format(event.time)
                setBackgroundColor(ContextCompat.getColor(context, event.color.color))
            }

            binding.itemMelodyNumberText.text = event.melody.number.toString()
            binding.itemMelodyNameText.text = event.melody.name

            //on click, goes to the add event fragment passing safe args
            itemView.setOnClickListener{
                val time = event.time.toLocalTime().toString()
                val date = event.time.toLocalDate().toString()
                val melody = event.melody.number - 1
                val color = event.color.color
                val action = MonthViewFragmentDirections.actionMonthViewFragmentToAddEventFragment(time,date,melody,color)
                binding.root.findNavController().navigate(action)
            }
        }
    }
}