package it.fnorg.bellapp.calendar_activity.monthview

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
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

            binding.itemMelodyNumberText.text = event.melodyNumber.toString()
            binding.itemMelodyNameText.text = event.melodyName

            val indexOfEvent = viewModel.events.value?.indexOfFirst { it.id == event.id }

            if (indexOfEvent != null) {
                if (indexOfEvent >= (viewModel.oldEventsStartingIndex)) {
                    binding.deleteEvent.visibility = View.VISIBLE
                    binding.itemEventDateText.setBackgroundColor(ContextCompat.getColor(mContext, R.color.silver))

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
                    binding.deleteEvent.visibility = View.GONE

                    // On click, goes to the add event fragment passing safe args
                    itemView.setOnClickListener {
                        if (!isInternetAvailable(mContext)) {
                            Toast.makeText(mContext, mContext.getString(R.string.connection_warning_1), Toast.LENGTH_SHORT).show()
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
    }
}