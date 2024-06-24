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
                date.atTime(14, 0),
                3,
                "Ave Maria"
            ),
        )
        add(
            Event(
                date.atTime(21, 30),
                1,
                "Coldplay"
            ),
        )
    }
}

val eventDateTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEE'\n'dd MMM'\n'HH:mm")

class CalendarActivityViewModel : ViewModel() {

}