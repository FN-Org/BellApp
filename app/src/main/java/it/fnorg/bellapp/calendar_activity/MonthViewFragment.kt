package it.fnorg.bellapp.calendar_activity

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.navigation.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.nextMonth
import com.kizitonwose.calendar.core.previousMonth
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import it.fnorg.bellapp.R
import it.fnorg.bellapp.databinding.CalendarDayBinding
import it.fnorg.bellapp.databinding.CalendarFragmentMonthViewBinding
import it.fnorg.bellapp.databinding.CalendarHeaderBinding
import it.fnorg.bellapp.main_activity.MainActivity
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

class MonthViewFragment : Fragment() {

    val viewModel: CalendarActivityViewModel by activityViewModels()

    private var selectedDate: LocalDate? = null

    private lateinit var eventsAdapter: EventListAdapter
    private var events: Map<LocalDate, List<Event>> = emptyMap()

    private lateinit var binding: CalendarFragmentMonthViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = CalendarFragmentMonthViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val daysOfWeek = daysOfWeek(firstDayOfWeek = DayOfWeek.MONDAY)
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(200)
        val endMonth = currentMonth.plusMonths(200)

        eventsAdapter = EventListAdapter(requireContext(), this ,emptyList())
        binding.calendarRv.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = eventsAdapter
        }

        viewModel.events.observe(viewLifecycleOwner) { eventsList ->
            events = eventsList.groupBy { it.time.toLocalDateTime().toLocalDate() }
            updateAdapterForDate(selectedDate)
            configureBinders(daysOfWeek)
            binding.calendarView.notifyCalendarChanged() // Notify the calendar to update its views
        }

        binding.calendarView.setup(startMonth, endMonth, daysOfWeek.first())
        binding.calendarView.scrollToMonth(currentMonth)

        binding.calendarView.monthScrollListener = { month ->
            binding.calendarMonthYearText.text = month.yearMonth.displayText()

            selectedDate?.let {
                selectedDate = null
                binding.calendarView.notifyDateChanged(it)
                updateAdapterForDate(null)
            }
        }

        binding.calendarNextMonthImage.setOnClickListener {
            binding.calendarView.findFirstVisibleMonth()?.let {
                binding.calendarView.smoothScrollToMonth(it.yearMonth.nextMonth)
            }
        }

        binding.calendarPreviousMonthImage.setOnClickListener {
            binding.calendarView.findFirstVisibleMonth()?.let {
                binding.calendarView.smoothScrollToMonth(it.yearMonth.previousMonth)
            }
        }

        binding.calendarBackArrow.setOnClickListener {
            val intent = Intent(requireActivity(), MainActivity::class.java)
            startActivity(intent)
        }

        val addEventButton: FloatingActionButton = binding.root.findViewById(R.id.calendarAddEventButton)
        addEventButton.setOnClickListener {
            view.findNavController().navigate(R.id.action_monthViewFragment_to_addEventFragment)
        }

    }

    private fun updateAdapterForDate(date: LocalDate?) {
        val eventsForDate = events[date].orEmpty().sortedBy { it.time.toLocalDateTime().toLocalTime() }
        eventsAdapter = EventListAdapter(requireContext(), this, eventsForDate)
        binding.calendarRv.adapter = eventsAdapter
    }


    private fun configureBinders(daysOfWeek: List<DayOfWeek>) {
        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay
            val binding = CalendarDayBinding.bind(view)

            init {
                view.setOnClickListener {
                    if (day.position == DayPosition.MonthDate) {
                        if (selectedDate != day.date) {
                            val oldDate = selectedDate
                            selectedDate = day.date
                            this@MonthViewFragment.binding.calendarView.notifyDateChanged(day.date)
                            oldDate?.let {
                                this@MonthViewFragment.binding.calendarView.notifyDateChanged(it)
                            }
                            updateAdapterForDate(day.date)
                        }
                    }
                }
            }
        }

        binding.calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data
                val context = container.binding.root.context
                val textView = container.binding.calendarDayText
                val layout = container.binding.calendarDayLayout
                textView.text = data.date.dayOfMonth.toString()

                val eventTopView = container.binding.calendarDayEventTop
                val eventBottomView = container.binding.calendarDayEventBottom
                val eventsContainer = container.binding.containerMoreEvents
                eventTopView.background = null
                eventBottomView.background = null

                if (data.position == DayPosition.MonthDate) {
                    textView.setTextColor(ContextCompat.getColor(context, R.color.white))
                    layout.setBackgroundResource(if (selectedDate == data.date) R.drawable.calendar_day_selected else 0)

                    val events = this@MonthViewFragment.events[data.date]?.sortedBy { it.time.toLocalDateTime().toLocalTime() }
                    if (events != null) {
                        if (events.count() == 1) {
                            eventBottomView.setBackgroundColor(ContextCompat.getColor(context, viewModel.colorsList[events[0].color-1].color))
                        } else {
                            eventTopView.setBackgroundColor(ContextCompat.getColor(context, viewModel.colorsList[events[0].color-1].color))
                            eventBottomView.setBackgroundColor(ContextCompat.getColor(context, viewModel.colorsList[events[1].color-1].color))

                            if (events.count() > 2) {
                                eventsContainer.removeAllViews()
                                for (i in 2 until events.size) {
                                    val dot = View(context).apply {
                                        layoutParams = LinearLayout.LayoutParams(16, 16).apply {
                                            marginEnd = 8
                                        }
                                        setBackgroundColor(ContextCompat.getColor(context, viewModel.colorsList[events[i].color-1].color))
                                    }
                                    eventsContainer.addView(dot)
                                }
                            }
                        }
                    }
                } else {
                    textView.setTextColor(ContextCompat.getColor(context, R.color.naples))
                    layout.background = null
                }
            }
        }

        class MonthViewContainer(view: View) : ViewContainer(view) {
            val legendLayout = CalendarHeaderBinding.bind(view).legendLayout.root
        }

        val typeFace = Typeface.create("sans-serif-light", Typeface.BOLD)
        binding.calendarView.monthHeaderBinder =
            object : MonthHeaderFooterBinder<MonthViewContainer> {
                override fun create(view: View) = MonthViewContainer(view)
                override fun bind(container: MonthViewContainer, data: CalendarMonth) {
                    if (container.legendLayout.tag == null) {
                        container.legendLayout.tag = data.yearMonth
                        val context = container.legendLayout.context
                        container.legendLayout.children.map { it as TextView }
                            .forEachIndexed { index, tv ->
                                tv.text = daysOfWeek[index].displayText(uppercase = true)
                                tv.setTextColor(ContextCompat.getColor(context, R.color.blue))
                                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                                tv.typeface = typeFace
                            }
                    }
                }
            }
    }

    //Every time that it re-became visible, it does this
    override fun onResume() {
        super.onResume()

        viewModel.fetchEventsData()
        viewModel.fetchMelodiesData()

    }
}
