package it.fnorg.bellapp

import android.view.LayoutInflater
import android.view.ViewGroup
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
        fun bind(flight: Event) {
            binding.itemFlightDateText.apply {
                text = eventDateTimeFormatter.format(flight.time)
//                setBackgroundColor(itemView.context.getColorCompat(event.color))
            }

//            binding.itemDepartureAirportCodeText.text = flight.departure.code
//            binding.itemDepartureAirportCityText.text = flight.departure.city
//
//            binding.itemDestinationAirportCodeText.text = flight.destination.code
//            binding.itemDestinationAirportCityText.text = flight.destination.city
        }
    }
}