package it.fnorg.bellapp.calendar_activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import it.fnorg.bellapp.R

class ColorAdapter(
        context: Context,
        private val options: List<Color>
    ) : ArrayAdapter<Color>(context, 0, options) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            return createView(position, convertView, parent)
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            return createView(position, convertView, parent)
        }

        private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.calendar_spinner_colors, parent, false)

            val colorNameTextView: TextView = view.findViewById(R.id.colorName)
            val colorPreviewView: View = view.findViewById(R.id.colorPreview)

            val option = options[position]
            colorNameTextView.text = option.name
            colorPreviewView.background.setTint(ContextCompat.getColor(context, option.color))

            return view
        }
    }