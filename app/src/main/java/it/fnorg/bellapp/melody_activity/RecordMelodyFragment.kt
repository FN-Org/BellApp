package it.fnorg.bellapp.melody_activity

import android.app.AlertDialog
import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
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
import androidx.navigation.fragment.findNavController
import it.fnorg.bellapp.R
import it.fnorg.bellapp.databinding.MelodyFragmentRecordMelodyBinding
import java.io.File

/**
 * A simple [Fragment] subclass.
 * Use the [RecordMelodyFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RecordMelodyFragment : Fragment() {

    private lateinit var binding: MelodyFragmentRecordMelodyBinding
    private lateinit var soundPool: SoundPool
    private val notes = listOf("C", "D", "E", "F", "G", "A", "B")
    private val soundMap = mutableMapOf<String, Int>()
    private val handler = Handler(Looper.getMainLooper())
    private var numBells: Int = 0
    private var isRecording = false
    private var startTime: Double = 0.0
    private val recordList = mutableListOf<String>()
    private var recordTitle: String = ""
    private var lastBell: String? = null
    private var lastClickTime: Double? = null
    private var playbackHandler: Handler? = null
    private var playbackRunnable: Runnable? = null
    private var isPlaying = false
    private var isPaused = false
    private var pauseTime: Double = 0.0
    private var bipSoundId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        binding.backArrow.setOnClickListener {
            navController.navigate(R.id.action_recordMelodyFragment_to_personalMelodiesFragment)
        }

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

        binding.saveMelodyButton.setOnClickListener {
            showSaveMelodyDialog()
        }

        binding.play.setOnClickListener {
            if (recordList.isNotEmpty() && !isPlaying) {
                if (isPaused) resumePlayback()
                else startPlayback()
            }
        }

        binding.pause.setOnClickListener {
            if (isPlaying) {
                pausePlayback()
            }
        }

        binding.stop.setOnClickListener {
            stopPlayback()
        }

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
        numBells = activity?.intent?.getIntExtra("NUM_BELLS", 0) ?: 0
        generateBellButtons(numBells)

        // SoundPool initialization
        soundPool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            SoundPool.Builder()
                .setMaxStreams(6)
                .setAudioAttributes(audioAttributes)
                .build()
        } else {
            SoundPool(6, AudioManager.STREAM_MUSIC, 0)
        }

        soundMap["C"] = soundPool.load(activity, R.raw.c4, 1)
        soundMap["D"] = soundPool.load(activity, R.raw.d4, 1)
        soundMap["E"] = soundPool.load(activity, R.raw.e4, 1)
        soundMap["F"] = soundPool.load(activity, R.raw.f4, 1)
        soundMap["G"] = soundPool.load(activity, R.raw.g4, 1)

        // Countdown sound
        bipSoundId = soundPool.load(activity, R.raw.countdown, 1)
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

        val numRows = (numBells + numColumns - 1) / numColumns

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
                    text = notes[bellsAdded % notes.size]
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
            val note = notes[bellNumber - 1]
            val soundId = soundMap[note] ?: return
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
            startBellAnimation(button)

            // Play the animation
            handler.postDelayed({
                soundPool.stop(soundId)
            }, 500)

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
        soundPool.play(bipSoundId, 1f, 1f, 1, 0, 1f)

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
                // updateControlPanelButtonsState(true)
            }
        }

        updateCountdown(countdownTime)
        // updateControlPanelButtonsState(false)
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
            val elapsedTime = (currentTime - lastClickTime!!)
            val recordEntry = "$lastBell $elapsedTime\n"
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

        // Se c'è una campana registrata, aggiungila alla lista senza tempo intercorso
        if (lastBell != null && lastClickTime != null) {
            val recordEntry = "$lastBell 1\n"
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

    private fun saveRecordToFile() {
        val fileName = "bell_record_${System.currentTimeMillis()}.txt"
        val file = File(requireContext().filesDir, fileName)

        val content = buildString {
            appendLine(recordTitle)
            append(recordList.joinToString(""))
        }

        file.writeText(content)
        recordList.clear()
        recordTitle = ""
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
                recordTitle = title

                saveRecordToFile()

                dialog.dismiss()

                val navController = findNavController()
                navController.navigate(R.id.action_recordMelodyFragment_to_personalMelodiesFragment)
                Toast.makeText(requireContext(),getString(R.string.melody_created),Toast.LENGTH_SHORT).show()

            } else {
                input.error = "The title can't be empty"
            }
        }

        builder.setNegativeButton("CANCEL") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun startPlayback() {
        isPlaying = true
        isPaused = false
        playbackHandler = Handler(Looper.getMainLooper())
        playbackRunnable = object : Runnable {
            private var index = 0

            override fun run() {
                if (index < recordList.size) {
                    val entry = recordList[index].trim().split(" ")
                    if (entry.size == 2) {
                        val note = entry[0]
                        val pauseDurationStr = entry[1]
                        val pauseDuration = try {
                            pauseDurationStr.toLong() * 1000 // Converts seconds in milliseconds
                        } catch (e: NumberFormatException) {
                            e.printStackTrace()
                            0
                        }

                        if (pauseDuration >= 0) {
                            playNote(note)

                            // Stops the note after 500ms
                            playbackHandler?.postDelayed({
                                stopNote()
                                index++

                                // Wait for the write duration
                                playbackHandler?.postDelayed(this, pauseDuration)
                            }, 500) // La durata della nota è sempre 500ms
                        } else {
                            index++
                            playbackHandler?.post(this)
                        }
                    } else {
                        index++
                        playbackHandler?.post(this)
                    }
                } else {
                    stopPlayback()
                }
            }

        }

        // Activate the runnable
        playbackHandler?.post(playbackRunnable!!)
    }

    private fun playNote(note: String) {
        val noteIndex = note.toIntOrNull()

        // Convert from bell number to bell note
        if (noteIndex != null && noteIndex in 1..notes.size) {
            val musicalNote = notes[noteIndex - 1]
            val soundId = soundMap[musicalNote] ?: return
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
        } else {
            Log.e("Playback", "Nota non valida: $note")
        }
    }


    private fun stopNote() {
        soundPool.autoPause() // Pause all sounds if needed
    }

    private fun pausePlayback() {
        isPaused = true
        isPlaying = false
        pauseTime = System.currentTimeMillis() / 1000.0
        playbackHandler?.removeCallbacks(playbackRunnable!!)
        stopNote()
    }

    private fun resumePlayback() {
        isPlaying = true
        isPaused = false
        val resumeDelay = System.currentTimeMillis() / 1000.0 - pauseTime
        playbackHandler?.postDelayed(playbackRunnable!!, resumeDelay.toLong())
    }

    private fun stopPlayback() {
        isPlaying = false
        isPaused = false
        playbackHandler?.removeCallbacks(playbackRunnable!!)
        stopNote() // Ensure that playback stops
        playbackHandler = null
        playbackRunnable = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        soundPool.release()
        playbackHandler?.removeCallbacks(playbackRunnable!!)
    }
}
