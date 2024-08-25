package it.fnorg.bellapp.melody_activity

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.storage.FirebaseStorage

data class MelodyFile(
    val number: Int = 0,
    val title: String = "",
    val recordList: MutableList<String> = mutableListOf()
)

class MelodyViewModel : ViewModel() {

    private val _melodyList = MutableLiveData<List<MelodyFile>>()
    val melodyList: LiveData<List<MelodyFile>> get() = _melodyList

    var sysId: String = ""
    var nBells: Int = 0

    // Firebase Storage reference
    private val storageReference = FirebaseStorage.getInstance().reference

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
}