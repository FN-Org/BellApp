package it.fnorg.bellapp.melody_activity.recordmelody

import android.app.AlertDialog
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Space
import android.widget.TableRow
import android.widget.Toast
import androidx.core.view.children
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.storage.FirebaseStorage
import it.fnorg.bellapp.R
import it.fnorg.bellapp.databinding.MelodyFragmentRecordMelodyBinding
import it.fnorg.bellapp.melody_activity.MelodyViewModel
import java.io.File

/**
 * A simple [Fragment] subclass.
 * Use the [RecordMelodyFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RecordMelodyFragment : Fragment() {

    private lateinit var binding: MelodyFragmentRecordMelodyBinding
    private val handler = Handler(Looper.getMainLooper())
    private var isRecording = false
    private var startTime: Double = 0.0
    private val recordList = mutableListOf<String>()
    private var lastBell: String? = null
    private var lastClickTime: Double? = null
    private var bipSoundId: Int = 0

    private val viewModel: MelodyViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(requireContext(), R.string.portrait_orientation, Toast.LENGTH_SHORT).show()
        }
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MelodyFragmentRecordMelodyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()

        viewModel.fetchMelodies()

        // Navbar buttons
        binding.backArrow.setOnClickListener {
            navController.navigate(R.id.action_recordMelodyFragment_to_personalMelodiesFragment)
        }

        binding.saveMelodyButton.setOnClickListener {
            showSaveMelodyDialog()
        }

        // Control panel buttons
        binding.micPlay.setOnClickListener {
            if (!isRecording) {
                recordList.clear()
                startCountdownAndRecording()
            }
        }

        binding.micStop.setOnClickListener {
            if (isRecording) {
                stopRecording()
            }
        }

        binding.play.setOnClickListener {
            if (recordList.isNotEmpty() && !viewModel.isPlaying) {
                val dButtons = listOf(binding.micStop, binding.play, binding.micPlay)
                dButtons.forEach { button ->
                    disableButton(button)
                }
                val eButtons = listOf(binding.pause, binding.stop)
                eButtons.forEach { button ->
                    enableButton(button)
                }
                if (viewModel.isPaused) viewModel.resumePlayback()
                else viewModel.startPlayback(recordList, ::recordMelodyFragmentStopPlayback)
            }
        }

        binding.pause.setOnClickListener {
            if (viewModel.isPlaying) {
                val dButtons = listOf(binding.micStop, binding.pause, binding.micPlay)
                dButtons.forEach { button ->
                    disableButton(button)
                }
                val eButtons = listOf(binding.play, binding.stop)
                eButtons.forEach { button ->
                    enableButton(button)
                }
                viewModel.pausePlayback()
            }
        }

        binding.stop.setOnClickListener {
            recordMelodyFragmentStopPlayback()
        }

        // New melody button
        binding.melodyBin.setOnClickListener {
            recordList.clear()
            binding.newMelodyLayout.visibility = View.GONE
            binding.saveMelodyButton.visibility = View.GONE
            val buttons = listOf(binding.micStop, binding.pause, binding.play, binding.stop)
            buttons.forEach { button ->
                disableButton(button)
            }
        }

        // Disable unusable control panel buttons
        val buttons = listOf(binding.micStop, binding.pause, binding.play, binding.stop)
        buttons.forEach { button ->
            disableButton(button)
        }

        // Bells generation
        generateBellButtons(viewModel.nBells)

        // Countdown sound
        bipSoundId = viewModel.soundPool.load(activity, R.raw.countdown, 1)
    }

    private fun recordMelodyFragmentStopPlayback() {
        // Disable and enable buttons
        val dButtons = listOf(binding.micStop, binding.pause, binding.stop)
        dButtons.forEach { button ->
            disableButton(button)
        }
        val eButtons = listOf(binding.play, binding.micPlay)
        eButtons.forEach { button ->
            enableButton(button)
        }
        viewModel.stopPlayback()
    }

    private fun generateBellButtons(numBells: Int) {
        val bellTable = binding.bellsTable
        bellTable.removeAllViews()

        val minColumns = 2
        val maxColumns = 4

        val numColumns = when {
            numBells <= 4 -> minColumns
            numBells <= 9 -> 3
            else -> maxColumns
        }

        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val buttonSize = (screenWidth / numColumns) - 2 * 8

        val maxButtonSize = (screenWidth / minColumns) - 2 * 8
        val finalButtonSize = minOf(buttonSize, maxButtonSize)

        var bellsAdded = 0

        while (bellsAdded < numBells) {
            val tableRow = TableRow(activity)
            tableRow.layoutParams = TableRow.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            tableRow.gravity = Gravity.CENTER

            val remainingBells = numBells - bellsAdded
            val columnsInRow = if (remainingBells < numColumns) remainingBells else numColumns

            for (col in 0 until columnsInRow) {
                val bellNumber = bellsAdded + 1
                val bellButton = Button(activity).apply {
                    text = viewModel.notes[bellsAdded % viewModel.notes.size]
                    tag = bellNumber
                    setBackgroundResource(R.drawable.ic_bell)
                    setTextColor(Color.WHITE)
                    textSize = 18f
                    setPadding(8, 8, 8, 8)
                    setOnClickListener { onBellClick(it as Button) }
                }

                val params = TableRow.LayoutParams(
                    finalButtonSize,
                    finalButtonSize
                ).apply {
                    setMargins(8, 8, 8, 8)
                }
                bellButton.layoutParams = params

                tableRow.addView(bellButton)
                bellsAdded++
            }

            for (col in columnsInRow until numColumns) {
                val emptySpace = Space(activity)
                val params = TableRow.LayoutParams(
                    finalButtonSize,
                    finalButtonSize
                )
                emptySpace.layoutParams = params
                tableRow.addView(emptySpace)
            }

            bellTable.addView(tableRow)
        }
    }

    private fun onBellClick(button: Button) {
        if (isRecording) {
            // Save the clicked bell (note)
            val bellNumber = button.tag as Int
            recordBellClick(bellNumber.toString())

            // Play the note
            val note = viewModel.notes[bellNumber - 1]
            val soundId = viewModel.soundMap[note] ?: return
            val streamId = viewModel.soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
            // 500ms duration of the note sound
            handler.postDelayed({
                viewModel.soundPool.stop(streamId)
            }, 500)

            // Play the animation
            startBellAnimation(button)
        }
    }

    private fun startBellAnimation(button: Button) {
        val scaleAnimation = ScaleAnimation(
            1f, 1.1f, 1f, 1.1f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 500
            repeatMode = ScaleAnimation.REVERSE
            repeatCount = 1
        }

        button.startAnimation(scaleAnimation)
    }

    private fun startCountdownAndRecording() {
        binding.countdownTv.visibility = View.VISIBLE
        val countdownTime = 3
        val countdownHandler = Handler(Looper.getMainLooper())

        binding.saveMelodyButton.visibility = View.GONE
        binding.newMelodyLayout.visibility = View.GONE

        // Disable all the control panel buttons
        val buttons = listOf(binding.micPlay, binding.micStop, binding.pause, binding.play, binding.stop)
        buttons.forEach { button ->
            disableButton(button)
        }

        // Start the countdown effect
        viewModel.soundPool.play(bipSoundId, 1f, 1f, 1, 0, 1f)

        fun updateCountdown(timeLeft: Int) {
            if (timeLeft > 0) {
                binding.countdownTv.text = timeLeft.toString()
                countdownHandler.postDelayed({ updateCountdown(timeLeft - 1) }, 1000)
            } else {
                binding.countdownTv.text = "REC"
                startTime = System.currentTimeMillis() / 1000.0
                isRecording = true
                enableBellButtons(true)
                enableButton(binding.micStop)
            }
        }

        updateCountdown(countdownTime)
    }

    private fun disableButton(button: ImageView) {
        button.isEnabled = false
        button.setColorFilter(resources.getColor(R.color.gray, null))
    }

    private fun enableButton(button: ImageView) {
        button.isEnabled = true
        button.setColorFilter(resources.getColor(R.color.black, null))
    }

    private fun recordBellClick(bellNumber: String) {
        val currentTime: Double = System.currentTimeMillis() / 1000.0

        if (lastClickTime != null && lastBell != null) {

            val elapsedTimeSeconds = currentTime - lastClickTime!!
            val elapsedTimeRounded = String.format("%.1f", elapsedTimeSeconds)

            val recordEntry = "$lastBell $elapsedTimeRounded"
            recordList.add(recordEntry)
        }

        lastBell = bellNumber
        lastClickTime = currentTime
    }

    private fun stopRecording() {
        binding.countdownTv.visibility = View.GONE
        binding.saveMelodyButton.visibility = View.VISIBLE
        binding.newMelodyLayout.visibility = View.VISIBLE
        isRecording = false
        enableBellButtons(false)

        if (lastBell != null && lastClickTime != null) {
            val recordEntry = "$lastBell 1"
            recordList.add(recordEntry)
        }

        val buttons = listOf(binding.micPlay, binding.pause, binding.play, binding.stop)
        buttons.forEach { button ->
            enableButton(button)
        }
        disableButton(binding.micStop)

        // Reset variables
        lastBell = null
        lastClickTime = null
    }

    private fun saveRecordToFile(recordTitle: String): File? {
        // Create the file
        val personalMelodiesNum = if (viewModel.melodyList.value.isNullOrEmpty()) {
            1
        } else {
            viewModel.melodyList.value?.size!! + 1
        }
        val fileName = "${personalMelodiesNum}.txt"
        val file = File(requireContext().filesDir, fileName)

        val content = buildString {
            appendLine(recordTitle)
            recordList.forEach {
                val formattedLine = it.replace(',', '.') // Assicura che il separatore decimale sia un punto
                appendLine(formattedLine)
            }
        }

        return try {
            file.writeText(content)
            recordList.clear()
            file // Return the file
        } catch (e: Exception) {
            Log.e("SaveRecordToFile", "Failed to save record to file", e)
            null // Return null in case of error
        }
    }

    private fun enableBellButtons(enabled: Boolean) {
        val bellButtons = binding.bellsTable.children
        for (button in bellButtons) {
            if (button is Button) {
                button.isEnabled = enabled
            }
        }
    }

    private fun showSaveMelodyDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Enter the title of the melody and click SAVE to save it")

        val input = EditText(requireContext())
        input.hint = "Insert the title"
        builder.setView(input)

        builder.setPositiveButton("SAVE") { dialog, _ ->
            val title = input.text.toString().trim()

            if (title.isNotEmpty()) {
                val newMelodyFile  = saveRecordToFile(title)
                if (newMelodyFile != null) {
                    val storageReference = FirebaseStorage.getInstance().reference
                    val fileRef = storageReference.child("melodies/${viewModel.sysId}/${newMelodyFile.name}")

                    fileRef.putFile(Uri.fromFile(newMelodyFile))
                        .addOnSuccessListener {
                            Log.d("FirebaseUpload", "Upload successful")
                            newMelodyFile.delete() // Delete the local file

                            // If the file is uploaded correctly
                            dialog.dismiss()
                            val navController = findNavController()
                            navController.navigate(R.id.action_recordMelodyFragment_to_personalMelodiesFragment)
                            Toast.makeText(requireContext(),getString(R.string.melody_created),Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.e("FirebaseUpload", "Upload failed", e)

                            // If the file is NOT uploaded correctly
                            dialog.dismiss()
                            val navController = findNavController()
                            navController.navigate(R.id.action_recordMelodyFragment_to_personalMelodiesFragment)
                            Toast.makeText(requireContext(),getString(R.string.sww_try_again),Toast.LENGTH_SHORT).show()
                        }
                }
            } else {
                input.error = "The title can't be empty"
            }
        }

        builder.setNegativeButton("CANCEL") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.stopPlayback()
    }

    override fun onPause() {
        super.onPause()
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }
}
