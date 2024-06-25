package it.fnorg.bellapp

import androidx.lifecycle.ViewModel
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

data class Event(
    val time: LocalDateTime,
    val melody: Int,
    val name: String
)

fun generateEvents(): List<Event> = buildList {
    val currentMonth = YearMonth.now()

    currentMonth.atDay(17).also { date ->
        add(
            Event(
                date.atTime(10, 0),
                3,
                "Ave Maria"
            ),
        )
        add(
            Event(
                date.atTime(12, 30),
                5,
                "AC/DC"
            ),
        )
        add(
            Event(
                date.atTime(13, 30),
                5,
                "AC/DC"
            ),
        )
        add(
            Event(
                date.atTime(16, 30),
                5,
                "AC/DC"
            ),
        )
        add(
            Event(
                date.atTime(17, 30),
                1,
                "Coldplay"
            ),
        )
        add(
            Event(
                date.atTime(21, 30),
                2,
                "Trilli"
            ),
        )
        add(
            Event(
                date.atTime(23, 30),
                5,
                "AC/DC"
            ),
        )
    }
}

val eventDateTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEE'\n'dd MMM'\n'HH:mm")

class CalendarActivityViewModel : ViewModel() {

}