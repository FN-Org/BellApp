package it.fnorg.bellapp.melody_activity

import android.media.SoundPool
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage

data class MelodyFile(
    val number: Int = 0,
    val title: String = "",
    val recordList: MutableList<String> = mutableListOf() // "Note Time"
)

class MelodyViewModel : ViewModel() {

    private val _melodyList = MutableLiveData<List<MelodyFile>>()
    val melodyList: LiveData<List<MelodyFile>> get() = _melodyList

    var sysId: String = ""
    var nBells: Int = 0

    var isSync: Boolean? = null

    // Firebase Storage reference
    private val storageReference = FirebaseStorage.getInstance().reference

    // Firebase Firestore reference
    private val db = Firebase.firestore

    // SoundPool
    lateinit var soundPool: SoundPool
    var soundMap = mutableMapOf<String, Int>()

    var isPlaying = false
    var isPaused = false
    var playbackHandler: Handler? = null
    var playbackRunnable: Runnable? = null
    val notes = listOf("C", "D", "E", "F", "G", "A", "B")
    private var pauseTime: Double = 0.0

    fun fetchMelodies() {
        val melodies = mutableListOf<MelodyFile>()

        val directoryRef = storageReference.child("melodies/${sysId}")

        directoryRef.listAll()
            .addOnSuccessListener { result ->
                // Loop through each file in the directory
                result.items.forEach { fileRef ->
                    fileRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { fileContent ->
                        // Convert file content to string and split into lines
                        val lines = fileContent.toString(Charsets.UTF_8).lines()

                        // The first line is the title
                        val title = lines.firstOrNull().orEmpty()

                        // The rest of the lines are the records
                        val records = lines.drop(1).toMutableList()

                        // Extract the melody number from the file name
                        val number = fileRef.name.removeSuffix(".txt").toIntOrNull() ?: 0

                        // Create a Melody object
                        val melody = MelodyFile(number, title, records)

                        // Add the melody to the list
                        melodies.add(melody)

                        // Update the LiveData
                        _melodyList.value = melodies

                    }.addOnFailureListener { e ->
                        Log.e("Download melody", "Failed to download file", e)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Download melody", "Failed to list files", e)
            }
    }

    fun startPlayback(recordList: MutableList<String>, stopFunction: () -> Unit) {
        isPlaying = true
        isPaused = false
        playbackHandler = Handler(Looper.getMainLooper())
        playbackRunnable = object : Runnable {
            private var index = 0

            override fun run() {
                if (!isPlaying) {
                    return
                }
                if (index < recordList.size) {
                    val entry = recordList[index].trim().split(" ")
                    if (entry.size == 2) {
                        val note = entry[0]
                        val pauseDurationStr = entry[1].replace(',', '.') // Converti la virgola in punto
                        var pauseDuration = try {
                            (pauseDurationStr.toDouble() * 1000).toLong() - 500 // Converti in millisecondi
                        } catch (e: NumberFormatException) {
                            e.printStackTrace()
                            0L
                        }

                        if (pauseDuration <= 0) {
                            pauseDuration = 1
                        }

                        val streamId = playNote(note)

                        playbackHandler?.postDelayed({
                            soundPool.stop(streamId)
                            index++

                            // Wait for the time between two notes
                            playbackHandler?.postDelayed(this, pauseDuration)
                        }, 500) // Note duration always 500ms (it is a bell)

                    } else {
                        index++
                        playbackHandler?.post(this)
                    }
                } else {
                    stopFunction()
                }
            }
        }

        // Activate the runnable
        playbackHandler?.post(playbackRunnable!!)
    }

    private fun playNote(note: String): Int {
        val noteIndex = note.toIntOrNull()

        // Convert from bell number to bell note
        if (noteIndex != null && noteIndex in 1..notes.size) {
            val musicalNote = notes[noteIndex - 1]
            val soundId = soundMap[musicalNote] ?: return 0
            val streamId = soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
            return streamId
        } else {
            Log.e("Playback", "Nota non valida: $note")
            return 0
        }
    }

    fun stopPlayback() {
        isPlaying = false
        isPaused = false
        playbackHandler?.removeCallbacks(playbackRunnable!!)
        soundPool.autoPause() // Ensure that playback stops
        playbackHandler = null
        playbackRunnable = null
    }

    fun pausePlayback() {
        if (isPlaying && !isPaused) {
            isPaused = true
            isPlaying = false
            pauseTime = System.currentTimeMillis() / 1000.0
            playbackHandler?.removeCallbacks(playbackRunnable!!)
            soundPool.autoPause()
        }
    }

    fun resumePlayback() {
        if (isPaused && !isPlaying) {
            isPlaying = true
            isPaused = false
            val resumeDelay = System.currentTimeMillis() / 1000.0 - pauseTime
            playbackHandler?.postDelayed(playbackRunnable!!, resumeDelay.toLong())
        }
    }

    fun getSystemSync(callback: (Boolean?) -> Unit) {
        if (sysId.isNotEmpty()) {
            val syncBoolRef = db.collection("systems").document(sysId)

            syncBoolRef.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        isSync = document.getBoolean("sync")
                        callback(isSync)
                    } else {
                        Log.e("MelodyViewModel - Firestore", "Error: document does not exist")
                        callback(null)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("MelodyViewModel - Firestore", "Error reading sync field of the document", exception)
                    callback(null)
                }
        } else {
            Log.w("MelodyViewModel", "sysId is empty")
            callback(null)
        }
    }


    fun setSystemSync(bool: Boolean, callback: (Boolean) -> Unit) {
        if (sysId.isNotEmpty()) {
            val syncBoolRef = db.collection("systems").document(sysId)

            syncBoolRef.update("sync", bool) // Update the database
                .addOnSuccessListener {
                    isSync = bool // Update the view model value
                    callback(true) // Operation successful
                }
                .addOnFailureListener { e ->
                    Log.e("MelodyViewModel - Firestore", "Error updating sync field in the system document", e)
                    callback(false) // Operation failed
                }
        } else {
            Log.w("MelodyViewModel", "sysId is empty")
            callback(false) // Operation failed due to empty sysId
        }
    }

}