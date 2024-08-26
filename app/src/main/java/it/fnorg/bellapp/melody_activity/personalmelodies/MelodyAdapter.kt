package it.fnorg.bellapp.melody_activity.personalmelodies

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import it.fnorg.bellapp.R
import it.fnorg.bellapp.databinding.MelodyPersonalListItemBinding
import it.fnorg.bellapp.melody_activity.MelodyFile

class MelodyAdapter(private val mContext: Context, private val melodyList: List<MelodyFile>) : RecyclerView.Adapter<MelodyAdapter.MelodyViewHolder>() {

    class MelodyViewHolder(val binding: MelodyPersonalListItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MelodyViewHolder {
        val binding = MelodyPersonalListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MelodyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MelodyViewHolder, position: Int) {
        val melody = melodyList[position]
        holder.binding.melodyNumTv.text = melody.number.toString()
        holder.binding.titleTv.text = melody.title

        holder.binding.playMelody.setOnClickListener {
            // Logica per il pulsante Play
        }

        holder.binding.deleteMelodyIv.setOnClickListener {
            val storageReference = FirebaseStorage.getInstance().reference

            val fileRef = storageReference.child("melodies/${melody.number}.txt")

            fileRef.delete().addOnSuccessListener {

                (melodyList as MutableList).removeAt(position)
                notifyItemRemoved(position)

                notifyItemRangeChanged(position, melodyList.size)

                Toast.makeText(mContext, R.string.melody_deleted, Toast.LENGTH_SHORT).show()

            }.addOnFailureListener { exception ->
                Log.e("MelodyAdapter", "Failed to delete file", exception)
                Toast.makeText(mContext, R.string.sww_try_again, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int {
        return melodyList.size
    }
}
