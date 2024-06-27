package it.fnorg.bellapp.calendar_activity

import android.util.Log
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import it.fnorg.bellapp.R
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

data class Event(
    var id: String = "",
    val time: Timestamp = Timestamp.now(),
    val melodyName: String = "",
    val melodyNumber: Int = 1,
    val color: Int = 1
)

data class Melody(
    val number: Int = 0,
    val name: String = ""
)

data class Color(
    val name: String = "",
    @ColorRes val color: Int = 0
)

val eventDateTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEE'\n'dd MMM'\n'HH:mm")

// Formattatori
val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

fun Timestamp.toLocalDateTime(): LocalDateTime {
    return this.toDate().toInstant().atZone(ZoneOffset.ofHours(2)).toLocalDateTime()
}

class CalendarActivityViewModel : ViewModel() {

    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> get() = _events

    private val _melodies = MutableLiveData<List<Melody>>()
    val melodies: LiveData<List<Melody>> get() = _melodies

    private val db = Firebase.firestore

    var sysId:String = ""

    // Initialize with an empty list
    init {
        _events.value = emptyList()
        _melodies.value = emptyList()
    }

    val colorsList = listOf(
        Color("Yellow", R.color.naples),
        Color("Red", R.color.lightred),
        Color("Green", R.color.jade)
    )

    fun fetchEventsData() {
        if (sysId.isEmpty()) {
            Log.w("BellAppDB", "sysId is empty, cannot fetch events.")
            return
        }
        db.collection("systems")
            .document(sysId)
            .collection("events")
            .get()
            .addOnSuccessListener { result ->
                val eventList = mutableListOf<Event>()
                for (document in result) {
                    document.toObject<Event>().let { event ->
                        eventList.add(event)
                    }
                }
                _events.value = eventList
                Log.w("BellAppDB", "Success events: $sysId")
            }
            .addOnFailureListener { exception ->
                Log.w("BellAppDB", "Error getting documents.", exception)
            }
    }

    fun fetchMelodiesData() {
        if (sysId.isEmpty()) {
            Log.w("BellAppDB", "sysId is empty, cannot fetch melodies.")
            return
        }
        db.collection("systems")
            .document(sysId)
            .collection("melodies")
            .get()
            .addOnSuccessListener { result ->
                val melodyList = mutableListOf<Melody>()
                for (document in result) {
                    document.toObject<Melody>().let { melody ->
                        melodyList.add(melody)
                    }
                }
                _melodies.value = melodyList
                Log.w("BellAppDB", "Success melodies: $sysId")
            }
            .addOnFailureListener { exception ->
                Log.w("BellAppDB", "Error getting documents.", exception)
            }
    }

    fun saveEvent(event: Event, identifier: String) {
        if (identifier == "default") {
            val newEvent = db.collection("systems")
                .document(sysId)
                .collection("events")
                .document()

            event.id = newEvent.id

            newEvent.set(event)
        }
        else {
            val modifiedEvent = db.collection("systems")
                .document(sysId)
                .collection("events")
                .document(identifier)

            // Update ref
            modifiedEvent.update("color", event.color,
                "melodyName", event.melodyName,
                                    "melodyNumber", event.melodyNumber,
                                    "time", event.time)
                .addOnSuccessListener {
                    // TODO: add Toast
                    Log.d("GradeTrackerDB", "Grade successfully updated!")
                }
                .addOnFailureListener { e ->
                    Log.w("GradeTrackerDB", "Error updating document", e)
                }
        }
    }
}