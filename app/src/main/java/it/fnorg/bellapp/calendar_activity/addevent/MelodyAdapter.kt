package it.fnorg.bellapp.calendar_activity.addevent

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import it.fnorg.bellapp.R
import it.fnorg.bellapp.calendar_activity.Melody

/**
 * Adapter for displaying a list of melodies in a spinner.
 *
 * @property context the context of the activity or specific application environment
 * @property options the list of available melodies
 */
class MelodyAdapter (
    context: Context,
    private val options: List<Melody>
) : ArrayAdapter<Melody>(context, 0, options) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.calendar_spinner_melodies, parent, false)

        // Find and configure the TextViews within the view
        val melodyNameTextView: TextView = view.findViewById(R.id.melodyName)
        val melodyNumberTextView: TextView = view.findViewById(R.id.melodyNumber)

        val option = options[position]

        // Set the text for the TextViews based on the melody item
        melodyNameTextView.text = option.name
        melodyNumberTextView.text = option.number.toString()

        return view
    }
}