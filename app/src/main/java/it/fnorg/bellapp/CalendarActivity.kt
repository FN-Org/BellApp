package it.fnorg.bellapp

import android.content.Intent
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
import it.fnorg.bellapp.main_activity.MainActivity
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

    }
}
