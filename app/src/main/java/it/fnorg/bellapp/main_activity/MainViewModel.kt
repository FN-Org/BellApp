package it.fnorg.bellapp.main_activity

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject

data class System(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    var numBells: Int = 1,
    var numMelodies : Int = 1,
    val pin: String = "",
    val timestamp: Timestamp = Timestamp.now()
)

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

    // Initialize with an empty list
    init {
        _systems.value = emptyList()
    }

    fun fetchSysData() {
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

    fun fetchUserData() {
        if (uid != null) {
            db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        _email.value = document.getString("email").toString()
                        _name.value = document.getString("fullName").toString()
                    }
                }
                .addOnFailureListener {

                }
        }
        else Log.d("HomeViewModelFetchUserData", "uid was null")
    }

    fun changeSysName(sysId : String,name : String){
        if (uid != null && name.isNotBlank()) {
            db.collection("users")
                .document(uid)
                .collection("systems")
                .document(sysId)
                .update("name", name)
                .addOnSuccessListener {

                }
                .addOnFailureListener { exception ->
                    Log.d("HomeViewModel", "change name failed with ", exception)
                }
        }
        else Log.d("HomeViewModelChangeSysName", "uid was null or name was blank")
    }

    fun fetchSysData(sysId: String, callback: (Boolean) -> Unit){
            db.collection("systems")
                .document(sysId)
                .get()
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
                    callback(false)
                }
    }

    fun addSys(sysId: String,location: String, name: String) {
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
}