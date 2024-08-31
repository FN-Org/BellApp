package it.fnorg.bellapp.melody_activity.personalmelodies

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import it.fnorg.bellapp.R
import it.fnorg.bellapp.calendar_activity.CalendarViewModel
import it.fnorg.bellapp.databinding.MelodyPersonalListItemBinding
import it.fnorg.bellapp.melody_activity.MelodyFile
import it.fnorg.bellapp.melody_activity.MelodyViewModel

class MelodyAdapter(
    private val mContext: Context,
    private var melodyList: List<MelodyFile>,
    private val melodyViewModel: MelodyViewModel,
    private val calendarViewModel: CalendarViewModel,
    private val lifecycleOwner: LifecycleOwner
) : RecyclerView.Adapter<MelodyAdapter.MelodyViewHolder>() {

    private var lastClickedMelody = -1
    private var lastClickedViewHolder: MelodyViewHolder? = null

    init {
        melodyList = melodyList.sortedBy { it.number }.toMutableList()
    }

    inner class MelodyViewHolder(val binding: MelodyPersonalListItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MelodyViewHolder {
        val binding = MelodyPersonalListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MelodyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MelodyViewHolder, position: Int) {
        val melody = melodyList[position]
        holder.binding.melodyNumTv.text = melody.number.toString()
        holder.binding.titleTv.text = melody.title

        // Observe the LiveData of the melody list
        calendarViewModel.melodies.observe(lifecycleOwner) { melodies ->
            // Check if the current melody is present in the list from the CalendarViewModel
            val isMelodyPresent = melodies.any { it.name == melody.title }
            // Update the visibility of the TextView
            holder.binding.syncMessage.visibility = if (isMelodyPresent) View.GONE else View.VISIBLE
        }

        holder.binding.playMelody.setOnClickListener {
            lastClickedViewHolder = holder
            val currentPosition = position
            if (melodyList[currentPosition].recordList.isNotEmpty() &&
                !melodyViewModel.isPlaying
            ) {
                if (melodyViewModel.isPaused && currentPosition == lastClickedMelody) {
                    melodyViewModel.resumePlayback()
                } else {
                    melodyViewModel.startPlayback(melodyList[currentPosition].recordList) {
                        personalMelodiesFragmentStopPlayback(holder)
                    }
                }

                lastClickedMelody = currentPosition
                holder.binding.playMelody.visibility = View.GONE
                holder.binding.pauseMelody.visibility = View.VISIBLE
            }
        }

        holder.binding.pauseMelody.setOnClickListener {
            lastClickedViewHolder = null
            melodyViewModel.pausePlayback()
            holder.binding.playMelody.visibility = View.VISIBLE
            holder.binding.pauseMelody.visibility = View.GONE
        }

        holder.binding.deleteMelodyIv.setOnClickListener {
            lastClickedViewHolder?.let { personalMelodiesFragmentStopPlayback(it) }
            val builder = AlertDialog.Builder(mContext)
            builder.setTitle(R.string.delete_confirmation)
            builder.setMessage(mContext.getString(R.string.delete_melody_message, melody.title))

            builder.setPositiveButton(R.string.yes) { dialog, _ ->
                val storageReference = FirebaseStorage.getInstance().reference
                val fileRef = storageReference.child("melodies/${melodyViewModel.sysId}/${melody.number}.txt")

                fileRef.delete().addOnSuccessListener {
                    (melodyList as? MutableList)?.let {
                        it.removeAt(position)
                        renameFilesAfterDeletion(position)
                        updateMelodyNumbers()
                        notifyItemRangeChanged(position, it.size)
                        Toast.makeText(mContext, R.string.melody_deleted, Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { exception ->
                    Log.e("MelodyAdapter", "Failed to delete file", exception)
                    Toast.makeText(mContext, R.string.sww_try_again, Toast.LENGTH_SHORT).show()
                }

                dialog.dismiss()
            }

            builder.setNegativeButton(R.string.no) { dialog, _ ->
                dialog.dismiss()
            }

            val alert = builder.create()
            alert.show()
        }

    }

    override fun getItemCount(): Int {
        return melodyList.size
    }

    private fun personalMelodiesFragmentStopPlayback(holder: MelodyViewHolder) {
        holder.binding.playMelody.visibility = View.VISIBLE
        holder.binding.pauseMelody.visibility = View.GONE
        melodyViewModel.stopPlayback()
    }

    private fun downloadFile(fileRef: StorageReference, callback: (ByteArray) -> Unit) {
        fileRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
            callback(bytes)
        }.addOnFailureListener { exception ->
            Log.e("MelodyAdapter", "Failed to download file", exception)
        }
    }

    private fun uploadFile(newFileRef: StorageReference, fileBytes: ByteArray) {
        newFileRef.putBytes(fileBytes).addOnSuccessListener {
            Log.d("MelodyAdapter", "File uploaded successfully")
        }.addOnFailureListener { exception ->
            Log.e("MelodyAdapter", "Failed to upload file", exception)
        }
    }

    private fun deleteOriginalFile(fileRef: StorageReference) {
        fileRef.delete().addOnSuccessListener {
            Log.d("MelodyAdapter", "Original file deleted successfully")
        }.addOnFailureListener { exception ->
            Log.e("MelodyAdapter", "Failed to delete original file", exception)
        }
    }

    private fun renameFilesAfterDeletion(deletedPosition: Int) {
        val storage = FirebaseStorage.getInstance()
        val storageReference = storage.reference

        val filesToRename = melodyList.drop(deletedPosition).map { melody ->
            storageReference.child("melodies/${melodyViewModel.sysId}/${melody.number}.txt")
        }

        filesToRename.forEachIndexed { index, fileRef ->
            val newFileName = "${deletedPosition + index + 1}.txt"
            val newFileRef = storageReference.child("melodies/${melodyViewModel.sysId}/$newFileName")

            // Scarica il file esistente
            downloadFile(fileRef) { fileBytes ->
                // Carica il file con il nuovo nome
                uploadFile(newFileRef, fileBytes)
                // Elimina il file originale
                deleteOriginalFile(fileRef)
            }
        }
    }

    private fun updateMelodyNumbers() {
        // Crea una nuova lista con i numeri aggiornati
        val updatedMelodyList = melodyList.mapIndexed { index, melody ->
            melody.copy(number = index + 1)
        }.sortedBy { it.number }.toMutableList()

        // Usa una nuova lista temporanea per aggiornare l'adapter
        val tempMelodyList = updatedMelodyList.toMutableList()
        (melodyList as MutableList).clear()
        (melodyList as MutableList).addAll(tempMelodyList)

        // Notifica l'adapter del cambiamento
        notifyDataSetChanged()
    }

}
