package it.fnorg.bellapp.main_activity.home

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import it.fnorg.bellapp.melody_activity.MelodyActivity
import it.fnorg.bellapp.R
import it.fnorg.bellapp.calendar_activity.CalendarActivity
import it.fnorg.bellapp.databinding.MainHomeListItemBinding
import it.fnorg.bellapp.isInternetAvailable
import it.fnorg.bellapp.main_activity.System
import it.fnorg.bellapp.main_activity.MainActivity
import it.fnorg.bellapp.main_activity.MainViewModel
import it.fnorg.bellapp.removeFCMTokenFromSystem

/**
 * Adapter for the RecyclerView in the home screen, responsible for displaying
 * a list of systems and handling user interactions with each system item.
 *
 * @property mContext Context of the adapter.
 * @property sysList List of systems to display.
 * @property viewModel ViewModel instance used for system operations.
 */
class HomeListAdapter (
    private val mContext: Context,
    private val sysList: List<System>,
    private val viewModel: MainViewModel
) : RecyclerView.Adapter<HomeListAdapter.SysHolder>() {

    private var filteredSysList: List<System> = sysList

    /**
     * ViewHolder class for holding the views of each system item.
     *
     * @param binding View binding object for accessing views.
     */
    inner class SysHolder(val binding: MainHomeListItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SysHolder {
        val binding = MainHomeListItemBinding.inflate(LayoutInflater.from(mContext),
            parent, false)
        return SysHolder(binding)
    }

    override fun getItemCount(): Int {
        return filteredSysList.size
    }

    override fun onBindViewHolder(holder: SysHolder, position: Int)
    {
        val sys = filteredSysList[position]
        val binding = holder.binding

        if (sys.name.isNotBlank()) {
            binding.sysTitle.text = sys.name
        } else {
            binding.sysTitle.text = sys.id
        }

        // Handle click on calendar button to navigate to CalendarActivity
        binding.calendarButton.setOnClickListener {
            val intent = Intent(mContext, CalendarActivity::class.java).apply {
                putExtra("id", sys.id)
            }
            mContext.startActivity(intent)
        }

        // Handle click on edit icon to change system name
        binding.editIv.setOnClickListener {
            showChangeNameInputDialog(sys.id)
        }

        // Handle click on close icon to remove system
        binding.removeIv.setOnClickListener {
            if (!isInternetAvailable(mContext)) {
                Toast.makeText(mContext, mContext.getString(R.string.sww_connection), Toast.LENGTH_SHORT).show()
            } else {
                val builder = AlertDialog.Builder(mContext)
                builder.setTitle(R.string.remove_sys)
                // Set up the buttons
                builder.setPositiveButton(mContext.getString(R.string.yes).uppercase()) { dialog, which ->
                    removeFCMTokenFromSystem(sys.id)
                    viewModel.removeSys(mContext, sys.id)
                    viewModel.fetchSysHomeData()

                }
                builder.setNegativeButton(mContext.getString(R.string.no).uppercase()) { dialog, which ->
                    dialog.cancel()
                }
                builder.show()
            }
        }


        // Handle click on info button
        binding.infoIv.setOnClickListener{
            viewModel.fetchSysData(sys.id) { success ->
                if (success) {
                    showInfoDialog(viewModel.system.value!!)
                } else {
                   Toast.makeText(mContext, R.string.sww_try_again, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Handle click on play button
        binding.createMelodyButton.setOnClickListener {
            val intent = Intent(mContext, MelodyActivity::class.java)

            viewModel.fetchSysData(sys.id) { success ->
                if (success) {
                    intent.putExtra("SYS_ID", sys.id)
                    intent.putExtra("NUM_BELLS", viewModel.system.value!!.nBells)
                    mContext.startActivity(intent)
                } else {
                    Toast.makeText(mContext, R.string.sww_try_again, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Method to display an input dialog for changing the system name.
     *
     * @param sysId ID of the system for which name is to be changed.
     */
    private fun showChangeNameInputDialog(sysId: String) {
        val builder = AlertDialog.Builder(mContext)
        builder.setTitle(R.string.change_name)

        // Set up the input
        val input = EditText(mContext)
        builder.setView(input)

        // Set up the save and cancel buttons
        builder.setPositiveButton(mContext.getString(R.string.save).uppercase()) { dialog, which ->
            val newName = input.text.toString()
            if (newName.isNotBlank()) {
                val viewModel = (mContext as MainActivity).viewModel
                viewModel.changeSysName(sysId, newName)
                viewModel.fetchSysHomeData()
                Toast.makeText(mContext, R.string.name_changed, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(mContext, R.string.empty_name, Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton(mContext.getString(R.string.cancel).uppercase()) { dialog, which ->
            dialog.cancel()
        }
        builder.show()
    }

    private fun showInfoDialog(sys : System){

        val inflater = LayoutInflater.from(mContext)
        val dialogView = inflater.inflate(R.layout.main_home_info_dialog,null)
        val builder = AlertDialog.Builder(mContext)
            .setView(dialogView)
            .create()

        val closeButton = dialogView.findViewById<ImageView>(R.id.closeIv)
        val nameTv = dialogView.findViewById<TextView>(R.id.name_TV)
        val locationTv = dialogView.findViewById<TextView>(R.id.location_TV)
        val idTv = dialogView.findViewById<TextView>(R.id.id_TV)
        val numBellsTv = dialogView.findViewById<TextView>(R.id.num_bell_TV)
        val numMelTv = dialogView.findViewById<TextView>(R.id.num_melodies_TV)

        closeButton.setOnClickListener{
            builder.dismiss()
        }

        nameTv.text = sys.name
        locationTv.text = sys.location
        idTv.text = sys.id
        numBellsTv.text = sys.nBells.toString()
        numMelTv.text = sys.nMelodies.toString()

        builder.show()
    }

    fun filter(query: String) {
        val lowerCaseQuery = query.lowercase()

        filteredSysList = if (lowerCaseQuery.isEmpty()) {
            sysList
        } else {
            sysList.filter { it.name.lowercase().contains(lowerCaseQuery) }
        }

        notifyDataSetChanged()
    }
}