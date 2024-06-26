package it.fnorg.bellapp.calendar_activity

import androidx.annotation.ColorRes
import androidx.lifecycle.ViewModel
import it.fnorg.bellapp.R
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

data class Event(
    val time: LocalDateTime,
    val melody: Int,
    val name: String,
    @ColorRes val color: Int,
)

fun generateEvents(): List<Event> = buildList {
    val currentMonth = YearMonth.now()

    currentMonth.atDay(17).also { date ->
        add(
            Event(
                date.atTime(10, 0),
                3,
                "Ave Maria",
                R.color.naples
            ),
        )
        add(
            Event(
                date.atTime(12, 30),
                5,
                "AC/DC",
                R.color.lightred
            ),
        )
        add(
            Event(
                date.atTime(13, 30),
                5,
                "AC/DC",
                R.color.lightred
            ),
        )
        add(
            Event(
                date.atTime(16, 30),
                5,
                "AC/DC",
                R.color.lightred
            ),
        )
        add(
            Event(
                date.atTime(17, 30),
                1,
                "Coldplay",
                R.color.jade
            ),
        )
        add(
            Event(
                date.atTime(21, 30),
                2,
                "Trilli",
                R.color.jade
            ),
        )
        add(
            Event(
                date.atTime(23, 30),
                5,
                "AC/DC",
                R.color.lightred
            ),
        )
    }
}

val eventDateTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEE'\n'dd MMM'\n'HH:mm")

class MonthViewViewModel : ViewModel() {
    // TODO: Implement the ViewModel
}