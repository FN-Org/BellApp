package it.fnorg.bellapp.calendar_activity.monthview

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
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

// Adapter class to manage a list of events in a RecyclerView for a calendar view
class EventListAdapter(
    private val mContext: Context,
    private val fragment: Fragment,
    private val viewModel: CalendarViewModel,
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
                text = eventDateTimeFormatter.format(event.time.toLocalDateTime())
                setBackgroundColor(ContextCompat.getColor(context, viewModel.colorsList[event.color-1].color))
            }

            // Set the melody number and name for the event
            binding.itemMelodyNumberText.text = event.melodyNumber.toString()
            binding.itemMelodyNameText.text = event.melodyName

            // Find the index of the event in the list of events from the ViewModel
            val indexOfEvent = viewModel.events.value?.indexOfFirst { it.id == event.id }

            // Check if the event is an old event (by comparing its index)
            if (indexOfEvent != null) {
                if (indexOfEvent >= (viewModel.oldEventsStartingIndex)) {
                    // Show the delete button and set a different background color for old events
                    binding.deleteEvent.visibility = View.VISIBLE
                    binding.itemEventDateText.setBackgroundColor(ContextCompat.getColor(mContext, R.color.silver))

                    // Handle the delete event action when the delete button is clicked
                    binding.deleteEvent.setOnClickListener {
                        if (!isInternetAvailable(mContext)) {
                            Toast.makeText(mContext,R.string.sww_connection, Toast.LENGTH_SHORT).show()
                        } else {
                            val builder = AlertDialog.Builder(mContext)
                            builder.setTitle(R.string.delete_confirmation_event)
                            builder.setMessage(mContext.getString(R.string.delete_event_message, ))

                            builder.setPositiveButton(R.string.yes) { dialog, _ ->
                                viewModel.deleteOldEvent(event.id) { result ->
                                    if (result > 0) {
                                        Toast.makeText(mContext, R.string.deleted, Toast.LENGTH_SHORT).show()
                                        viewModel.fetchEventsData()
                                        notifyDataSetChanged()
                                    }
                                    else Toast.makeText(mContext,R.string.sww_try_again,Toast.LENGTH_SHORT).show()
                                }

                                dialog.dismiss()
                            }

                            builder.setNegativeButton(R.string.no) { dialog, _ ->
                                dialog.dismiss()
                            }

                            val alert = builder.create()
                            alert.show()

                        }
                    }
                }
                else {
                    // Hide the delete button for future events
                    binding.deleteEvent.visibility = View.GONE

                    // Set up an onClick listener for future events to navigate to event details
                    itemView.setOnClickListener {
                        if (!isInternetAvailable(mContext)) {
                            Toast.makeText(mContext, mContext.getString(R.string.connection_warning_1), Toast.LENGTH_SHORT).show()
                        }
                        else {
                            // Navigate to the AddEventFragment with event details passed as arguments
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
    }
}