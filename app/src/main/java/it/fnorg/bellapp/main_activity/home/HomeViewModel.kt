package it.fnorg.bellapp.main_activity.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject

data class System(
    val id: String = "",
    val name: String = "",
    val location: String = ""
)

class HomeViewModel : ViewModel() {

    private val _systems = MutableLiveData<List<System>>()
    val systems: LiveData<List<System>> get() = _systems

    // Initialize with an empty list
    init {
        _systems.value = emptyList()
    }

    fun fetchSysData(uid: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(uid).collection("systems")
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
}