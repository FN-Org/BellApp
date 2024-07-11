package it.fnorg.bellapp.main_activity

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import it.fnorg.bellapp.R

/**
 * Data class representing a system.
 *
 * @property id Unique identifier for the system.
 * @property name Name of the system.
 * @property location Location of the system.
 * @property nBells Number of bells in the system.
 * @property nMelodies Number of melodies in the system.
 * @property pin Pin code for the system.
 * @property timestamp Timestamp of the system creation.
 */
data class System(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    var nBells: Int = 1,
    var nMelodies : Int = 1,
    val pin: Int = 10,
    val timestamp: Timestamp = Timestamp.now()
)

/**
 * ViewModel class for the MainActivity.
 */
class MainViewModel : ViewModel() {

    private val db = Firebase.firestore

    private val _systems = MutableLiveData<List<System>>()
    val systems: LiveData<List<System>> get() = _systems

    private val _email = MutableLiveData<String>()
    val email: LiveData<String> get() = _email

    private val _name = MutableLiveData<String>()
    val name: LiveData<String> get() = _name

    val uid = FirebaseAuth.getInstance().currentUser?.uid

    private val _system = MutableLiveData<System>()
    val system : LiveData<System> get() = _system

    private val _userImage = MutableLiveData<Uri>()
    val userImage : LiveData<Uri> get() = _userImage

    init {
        // Initialize with an empty list
        _systems.value = emptyList()
        _system.value = System()
    }

    /**
     * Fetches system data for the home screen.
     */
    fun fetchSysHomeData() {
        if (uid != null) {
            db.collection("users")
                .document(uid)
                .collection("systems")
                .get()
                .addOnSuccessListener { result ->
                    val systemsList = mutableListOf<System>()
                    for (document in result) {
                        document.toObject<System>().let { system ->
                            systemsList.add(system)
                        }
                    }
                    _systems.value = systemsList
                }
                .addOnFailureListener { exception ->
                    Log.d("HomeViewModel", "get failed with ", exception)
                }
        }
        else Log.d("HomeViewModelFetchSysdata", "uid was null")
    }

    /**
     * Fetches user data including email, name, and profile image.
     */
    fun fetchUserData() {
        if (uid != null) {
            db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        _email.value = document.getString("email").toString()
                        _name.value = document.getString("fullName").toString()

                        // Fetch the user image from Firebase Storage
                        val storageRef = FirebaseStorage.getInstance().reference
                        val fileRef = storageRef.child("profile_images/${uid}.jpg")
                        fileRef.downloadUrl.addOnSuccessListener { uri ->
                            _userImage.value = uri
                        }
                        .addOnFailureListener {
                            // Failed to fetch the user image
                            _userImage.value = Uri.parse("android.resource://it.fnorg.bellapp/drawable/ic_profile_default")
                            Log.w("Image", "Uri. " + _userImage.value)
                        }
                    }
                }
                .addOnFailureListener {
                    // Failed to fetch the user data
                }
        }
        else Log.d("HomeViewModelFetchUserData", "uid was null")
    }

    /**
     * Changes the name of a system.
     *
     * @param sysId ID of the system.
     * @param name New name of the system.
     */
    fun changeSysName(sysId : String,name : String){
        if (uid != null && name.isNotBlank()) {
            db.collection("users")
                .document(uid)
                .collection("systems")
                .document(sysId)
                .update("name", name)
                .addOnSuccessListener {
                    // Successfully changed the name
                }
                .addOnFailureListener { exception ->
                    Log.d("HomeViewModel", "change name failed with ", exception)
                }
        }
        else Log.d("HomeViewModelChangeSysName", "uid was null or name was blank")
    }

    /**
     * Fetches data for a specific system.
     *
     * @param sysId ID of the system.
     * @param callback Callback function to handle the success status.
     */
    fun fetchSysData(sysId: String, callback: (Boolean) -> Unit){
            db.collection("systems")
                .document(sysId)
                .get(Source.SERVER)
                .addOnSuccessListener { result ->
                    result.toObject<System>().let { system ->
                        if (system != null) {
                            _system.value = system
                            callback(true)
                        }
                        else {
                            _system.value = System()
                            callback(false)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("MainViewModel", "get failed with ", exception)
                    _system.value = System()
                    callback(false)
                }
    }

    /**
     * Adds a new system to the user's collection.
     *
     * @param sysId ID of the system.
     * @param location Location of the system.
     * @param name Name of the system.
     */
    fun addSys(sysId: String, location: String, name: String) {
        val selectedFields = mapOf(
            "id" to sysId,
            "name" to name,
            "location" to location
        )
        if (uid != null) {
            db.collection("users")
                .document(uid)
                .collection("systems")
                .document(sysId)
                .set(selectedFields)
                .addOnSuccessListener {
                    Log.d("HomeViewModelAddSys", "Document successfully created")
                }
                .addOnFailureListener { e ->
                    Log.w("HomeViewModelAddSys", "Error writing document", e)
                }
        }
        else Log.d("HomeViewModelAddSys", "uid was null")
    }

    /**
     * Uploads an image to Firebase Storage.
     *
     * @param context Application context.
     * @param uri URI of the image to upload.
     */
    fun uploadImageToFirebase(context: Context, uri: Uri) {
        val storageRef = FirebaseStorage.getInstance().reference
        val fileRef = storageRef.child("profile_images/${uid}.jpg")
        fileRef.putFile(uri)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    _userImage.value = downloadUri
                    Toast.makeText(context, "Image updated successfully", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Image upload failed", e)
            }
    }

    /**
     * Removes a system from the user's collection.
     *
     * @param context Application context.
     * @param sysId ID of the system to remove.
     */
    fun removeSys(context: Context, sysId: String) {
        if (uid != null) {
            db.collection("users")
                .document(uid)
                .collection("systems")
                .document(sysId)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(context, context.getString(R.string.sys_removed), Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, context.getString(R.string.sww_try_again), Toast.LENGTH_SHORT).show()
                }
        }
    }
}