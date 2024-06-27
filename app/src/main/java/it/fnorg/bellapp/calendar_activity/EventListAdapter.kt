package it.fnorg.bellapp.calendar_activity

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import it.fnorg.bellapp.databinding.CalendarEventItemViewBinding

class EventListAdapter(
    context: Context,
    val events: List<Event>
) :
    RecyclerView.Adapter<EventListAdapter.EventsViewHolder>() {

    private val ViewModel: CalendarActivityViewModel by lazy {
        // Assicurati che il context sia un LifecycleOwner
        val activity = context as? FragmentActivity
            ?: throw IllegalStateException("Context is not a FragmentActivity")

        ViewModelProvider(activity)[CalendarActivityViewModel::class.java]
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
                setBackgroundColor(ContextCompat.getColor(context, ViewModel.colorsList[event.color-1].color))
            }

            binding.itemMelodyNumberText.text = event.melodyNumber.toString()
            binding.itemMelodyNameText.text = event.melodyName

            //on click, goes to the add event fragment passing safe args
            itemView.setOnClickListener{
                val time = event.time.toLocalDateTime().toLocalTime().toString()
                val date = event.time.toLocalDateTime().toLocalDate().toString()
                val melody = event.melodyNumber
                val color = event.color
                val action = MonthViewFragmentDirections.actionMonthViewFragmentToAddEventFragment(time,date,melody,color)
                binding.root.findNavController().navigate(action)
            }
        }
    }
}