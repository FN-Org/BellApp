package it.fnorg.bellapp.melody_activity

import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.Button
import android.widget.Space
import android.widget.TableRow
import androidx.navigation.fragment.findNavController
import it.fnorg.bellapp.R
import it.fnorg.bellapp.databinding.MainFragmentHomeBinding
import it.fnorg.bellapp.databinding.MelodyFragmentRecordMelodyBinding

/**
 * A simple [Fragment] subclass.
 * Use the [RecordMelodyFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RecordMelodyFragment : Fragment() {

    private lateinit var binding: MelodyFragmentRecordMelodyBinding
    private lateinit var soundPool: SoundPool
    private val soundMap = mutableMapOf<String, Int>()
    private val handler = Handler(Looper.getMainLooper())
    private var numBells: Int = 0

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

        // Inizializzazione di SoundPool
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

        numBells = activity?.intent?.getIntExtra("NUM_BELLS", 0) ?: 0
        generateBellButtons(numBells)
    }

    private fun generateBellButtons(numBells: Int) {
        val bellTable = binding.bellsTable
        bellTable.removeAllViews()

        val notes = listOf("C", "D", "E", "F", "G", "A", "B")
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
                val bellButton = Button(activity).apply {
                    text = notes[bellsAdded % notes.size]
                    tag = notes[bellsAdded % notes.size]
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
        val note = button.tag as String

        val soundId = soundMap[note] ?: return
        soundPool.play(soundId, 1f, 1f, 1, 0, 1f)

        startBellAnimation(button)

        handler.postDelayed({
            soundPool.stop(soundId)
        }, 500)
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

    override fun onDestroyView() {
        super.onDestroyView()
        soundPool.release()
    }
}