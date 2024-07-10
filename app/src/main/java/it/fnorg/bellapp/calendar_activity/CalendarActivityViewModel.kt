package it.fnorg.bellapp.calendar_activity

import android.content.Context
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

/**
 * ViewModel for managing calendar-related data and operations.
 */
class CalendarActivityViewModel : ViewModel() {

    // LiveData for events list
    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> get() = _events

    // LiveData for melodies list
    private val _melodies = MutableLiveData<List<Melody>>()
    val melodies: LiveData<List<Melody>> get() = _melodies

    // Firebase Firestore instance
    private val db = Firebase.firestore

    var sysId:String = ""

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

    /**
     * Fetches melody data from Firestore database.
     */
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

    /**
     * Saves or updates an event in the Firestore database.
     *
     * @param context The context used to display Toast messages.
     * @param event The event object to be saved or updated.
     * @param identifier The identifier of the event. If "default", a new event is created; otherwise, the existing event is updated.
     */
    fun saveEvent(context: Context, event: Event, identifier: String) {
        if (identifier == "default") {
            // Create a new event document
            val newEvent = db.collection("systems")
                .document(sysId)
                .collection("events")
                .document()

            event.id = newEvent.id

            newEvent.set(event)
                .addOnSuccessListener {
                    Toast.makeText(context, context.getString(R.string.event_created), Toast.LENGTH_SHORT).show()
                    Log.d("BellAppDB", "Event successfully created!")
                }
                .addOnFailureListener{
                    Toast.makeText(context, context.getString(R.string.sww_try_again), Toast.LENGTH_SHORT).show()
                    Log.d("BellAppDB", "Error creating event")
                }
        }
        else {
            // Update an existing event document
            val modifiedEvent = db.collection("systems")
                .document(sysId)
                .collection("events")
                .document(identifier)

            modifiedEvent.update("color", event.color,
                "melodyName", event.melodyName,
                                    "melodyNumber", event.melodyNumber,
                                    "time", event.time)
                .addOnSuccessListener {
                    Toast.makeText(context, context.getString(R.string.event_updated), Toast.LENGTH_SHORT).show()
                    Log.d("BellAppDB", "Event successfully updated!")
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, context.getString(R.string.sww_try_again), Toast.LENGTH_SHORT).show()
                    Log.w("BellAppDB", "Error updating document", e)
                }
        }
    }

    /**
     * Deletes an event from the Firestore database.
     *
     * @param eventId The ID of the event to be deleted.
     */
    fun deleteEvent(eventId: String) {
        db.collection("systems")
            .document(sysId)
            .collection("events")
            .document(eventId).delete()
            .addOnSuccessListener {
                Log.d("BellAppDB", "Event successfully deleted")
            }
            .addOnFailureListener{ e ->
                Log.w("BellAppDB", "Error deleting document", e)
            }
    }
}