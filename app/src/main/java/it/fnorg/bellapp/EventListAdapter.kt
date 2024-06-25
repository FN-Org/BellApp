package it.fnorg.bellapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import it.fnorg.bellapp.databinding.CalendarEventItemViewBinding

class EventListAdapter :
    RecyclerView.Adapter<EventListAdapter.EventsViewHolder>() {
    val events = mutableListOf<Event>()

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
                setBackgroundColor(ContextCompat.getColor(context, R.color.lapis))
            }

            binding.itemMelodyNumberText.text = event.melody.toString()
            binding.itemMelodyNameText.text = event.name
        }
    }
}