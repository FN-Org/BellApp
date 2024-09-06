package it.fnorg.bellapp.calendar_activity

import android.util.Log
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

// Data class representing an event
data class Event(
    var id: String = "",
    val time: Timestamp = Timestamp.now(),
    val melodyName: String = "",
    val melodyNumber: Int = 1,
    val color: Int = 1
)

// Data class representing a melody
data class Melody(
    val number: Int = 0,
    val name: String = ""
)

// Data class representing a color
data class Color(
    val name: String = "",
    @ColorRes val color: Int = 0
)

// Formatters
val eventDateTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEE'\n'dd MMM'\n'HH:mm")
val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

fun Timestamp.toLocalDateTime(): LocalDateTime {
    return this.toDate().toInstant().atZone(ZoneOffset.ofHours(2)).toLocalDateTime()
}

/**
 * ViewModel for managing calendar-related data and operations.
 */
class CalendarViewModel : ViewModel() {

    // LiveData for events list
    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> get() = _events

    // LiveData for melodies list
    private val _melodies = MutableLiveData<List<Melody>>()
    val melodies: LiveData<List<Melody>> get() = _melodies

    // Firebase Firestore instance
    private val db = Firebase.firestore

    var sysId: String = ""

    var oldEventsStartingIndex = 0;

    // Initialize LiveData lists with empty lists
    init {
        _events.value = emptyList()
        _melodies.value = emptyList()
    }

    // List of colors available for events
    val colorsList = listOf(
        Color("Yellow", R.color.naples),
        Color("Red", R.color.lightred),
        Color("Green", R.color.jade)
    )

    /**
     * Fetches event data from Firestore database.
     */
    fun fetchEventsData() {
        if (sysId.isEmpty()) {
            Log.w("CalendarViewModel - fetchEventsData", "sysId is empty, cannot fetch events.")
            return
        }
        val eventList = mutableListOf<Event>()
        db.collection("systems")
            .document(sysId)
            .collection("events")
            .get()
            .addOnSuccessListener { result1 ->
                for (document in result1) {
                    document.toObject<Event>().let { event ->
                        eventList.add(event)
                    }
                }
                _events.value = eventList
                oldEventsStartingIndex = eventList.size
                Log.w("BellAppDB", "Success events: $sysId")

                db.collection("systems")
                    .document(sysId)
                    .collection("oldEvents")
                    .get()
                    .addOnSuccessListener { result2 ->
                        for (document in result2) {
                            document.toObject<Event>().let { event ->
                                eventList.add(event)
                            }
                        }
                        _events.value = eventList
                        Log.w("BellAppDB", "Success events: $sysId")
                    }
                    .addOnFailureListener { exception ->
                        Log.w("CalendarViewModel - fetchEventsData", "Error getting old events documents.", exception)
                    }
            }
            .addOnFailureListener { exception ->
                Log.w("CalendarViewModel - fetchEventsData", "Error getting new events documents.", exception)
            }
    }

    /**
     * Fetches melody data from Firestore database.
     */
    fun fetchMelodiesData() {
        if (sysId.isEmpty()) {
            Log.w("CalendarViewModel - fetchMelodiesData", "sysId is empty, cannot fetch melodies.")
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
                Log.w("CalendarViewModel - fetchMelodiesData", "Success melodies: $sysId")
            }
            .addOnFailureListener { exception ->
                Log.w("CalendarViewModel - fetchMelodiesData", "Error getting documents.", exception)
            }
    }

    /**
     * Saves or updates an event in the Firestore database.
     *
     * @param context The context used to display Toast messages.
     * @param event The event object to be saved or updated.
     * @param identifier The identifier of the event. If "default", a new event is created; otherwise, the existing event is updated.
     */
    fun saveEvent(event: Event, identifier: String, callback: (Int) -> Unit) {
        if (identifier == "default") {
            val newEvent = db.collection("systems")
                .document(sysId)
                .collection("events")
                .document()

            event.id = newEvent.id

            newEvent.set(event)
                .addOnSuccessListener {

                    Log.d("CalendarViewModel - saveEvent", "Event successfully created!")
                    callback(1)
                }
                .addOnFailureListener {

                    Log.d("CalendarViewModel - saveEvent", "Error creating event")
                    callback(-1)
                }
        } else {
            val modifiedEvent = db.collection("systems")
                .document(sysId)
                .collection("events")
                .document(identifier)

            modifiedEvent.update("color", event.color,
                "melodyName", event.melodyName,
                "melodyNumber", event.melodyNumber,
                "time", event.time)
                .addOnSuccessListener {

                    Log.d("CalendarViewModel - saveEvent", "Event successfully updated!")
                    callback(2)
                }
                .addOnFailureListener { e ->

                    Log.w("CalendarViewModel - saveEvent", "Error updating document", e)
                    callback(-2)
                }
        }
    }

    /**
     * Deletes an event from the Firestore database.
     *
     * @param eventId The ID of the event to be deleted.
     */
    fun deleteEvent(eventId: String,callback: (Int) -> Unit) {
        db.collection("systems")
            .document(sysId)
            .collection("events")
            .document(eventId).delete()
            .addOnSuccessListener {
                Log.d("CalendarViewModel - deleteEvent", "Event successfully deleted from events")
                callback(1)
            }
            .addOnFailureListener{ e ->
                Log.w("CalendarViewModel - deleteEvent", "Error deleting document in events", e)
                callback(-1)
            }
    }

    // Function to delete an old event from the "oldEvents" collection in Firestore
    fun deleteOldEvent(eventId: String,callback: (Int) -> Unit){
        db.collection("systems")
            .document(sysId)
            .collection("oldEvents")
            .document(eventId).delete()
            .addOnSuccessListener {
                Log.d("CalendarViewModel - deleteOldEvent", "Event successfully deleted from oldEvents")

                _events.value = _events.value?.filter { it.id != eventId }
                callback(1)
            }
            .addOnFailureListener{ e ->
                Log.w("CalendarViewModel - deleteOldEvent", "Error deleting document in oldEvents", e)
                callback(-1)
            }
    }
}