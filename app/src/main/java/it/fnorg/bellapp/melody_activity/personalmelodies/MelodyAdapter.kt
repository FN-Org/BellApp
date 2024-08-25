package it.fnorg.bellapp.melody_activity.personalmelodies

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import it.fnorg.bellapp.databinding.MelodyPersonalListItemBinding
import it.fnorg.bellapp.melody_activity.MelodyFile

class MelodyAdapter(private val melodyList: List<MelodyFile>) : RecyclerView.Adapter<MelodyAdapter.MelodyViewHolder>() {

    class MelodyViewHolder(val binding: MelodyPersonalListItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MelodyViewHolder {
        val binding = MelodyPersonalListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MelodyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MelodyViewHolder, position: Int) {
        val melodyName = melodyList[position]
        holder.binding.melodyNumTv.text = melodyName.number.toString()
        holder.binding.titleTv.text = melodyName.title

        holder.binding.playMelody.setOnClickListener {
            // Logica per il pulsante Play
        }
        holder.binding.deleteMelodyIv.setOnClickListener {
            // Logica per il pulsante Delete
        }
    }

    override fun getItemCount(): Int {
        return melodyList.size
    }
}
