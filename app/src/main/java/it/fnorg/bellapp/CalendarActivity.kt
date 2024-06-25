package it.fnorg.bellapp

import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.nextMonth
import com.kizitonwose.calendar.core.previousMonth
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import it.fnorg.bellapp.databinding.CalendarActivityCalendarBinding
import it.fnorg.bellapp.databinding.CalendarDayBinding
import it.fnorg.bellapp.databinding.CalendarHeaderBinding
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

class CalendarActivity : AppCompatActivity() {

    private var selectedDate: LocalDate? = null

    private val eventsAdapter = EventListAdapter()
    private val events = generateEvents().groupBy { it.time.toLocalDate() }

    private lateinit var binding: CalendarActivityCalendarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CalendarActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.calendarRv.apply {
            layoutManager = LinearLayoutManager(this@CalendarActivity, RecyclerView.VERTICAL, false)
            adapter = eventsAdapter
        }
        eventsAdapter.notifyDataSetChanged()

        val daysOfWeek = daysOfWeek()
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(200)
        val endMonth = currentMonth.plusMonths(200)
        configureBinders(daysOfWeek)
        binding.calendarView.setup(startMonth, endMonth, daysOfWeek.first())
        binding.calendarView.scrollToMonth(currentMonth)

        binding.calendarView.monthScrollListener = { month ->
            binding.calendarMonthYearText.text = month.yearMonth.displayText()

            selectedDate?.let {
                // Clear selection if we scroll to a new month.
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
    }

    private fun updateAdapterForDate(date: LocalDate?) {
        eventsAdapter.events.clear()
        eventsAdapter.events.addAll(events[date].orEmpty())
        eventsAdapter.notifyDataSetChanged()
    }

    private fun configureBinders(daysOfWeek: List<DayOfWeek>) {
        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay // Will be set when this container is bound.
            val binding = CalendarDayBinding.bind(view)

            init {
                view.setOnClickListener {
                    if (day.position == DayPosition.MonthDate) {
                        if (selectedDate != day.date) {
                            val oldDate = selectedDate
                            selectedDate = day.date
                            val binding = this@CalendarActivity.binding
                            binding.calendarView.notifyDateChanged(day.date)
                            oldDate?.let { binding.calendarView.notifyDateChanged(it) }
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
                    textView.setTextColor(ContextCompat.getColor(context, R.color.white)) // Days of the current month text color
                    layout.setBackgroundResource(if (selectedDate == data.date) R.drawable.calendar_day_selected else 0)

                    val events = events[data.date]
                    if (events != null) {
                        if (events.count() == 1) {
                            eventBottomView.setBackgroundColor(ContextCompat.getColor(context, events[0].color))
                        }
                        else {
                            eventTopView.setBackgroundColor(ContextCompat.getColor(context, events[0].color))
                            eventBottomView.setBackgroundColor(ContextCompat.getColor(context, events[1].color))

                            if (events.count() > 2) {
                                eventsContainer.removeAllViews()
                                for (i in 2 until events.size) {
                                    val dot = View(this@CalendarActivity).apply {
                                        layoutParams = LinearLayout.LayoutParams(16, 16).apply {
                                            marginEnd = 8
                                        }
                                        setBackgroundColor(ContextCompat.getColor(context, events[i].color))
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

        val typeFace = Typeface.create("sans-serif-light", Typeface.NORMAL)
        binding.calendarView.monthHeaderBinder =
            object : MonthHeaderFooterBinder<MonthViewContainer> {
                override fun create(view: View) = MonthViewContainer(view)
                override fun bind(container: MonthViewContainer, data: CalendarMonth) {
                    // Setup each header day text if we have not done that already.
                    if (container.legendLayout.tag == null) {
                        container.legendLayout.tag = data.yearMonth
                        val context = container.legendLayout.context
                        container.legendLayout.children.map { it as TextView }
                            .forEachIndexed { index, tv ->
                                tv.text = daysOfWeek[index].displayText(uppercase = true)
                                tv.setTextColor(ContextCompat.getColor(context, R.color.white))
                                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                                tv.typeface = typeFace
                            }
                    }
                }
            }
    }
}
