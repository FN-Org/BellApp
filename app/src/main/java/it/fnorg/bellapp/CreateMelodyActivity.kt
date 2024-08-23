package it.fnorg.bellapp

import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.Button
import android.widget.Space
import android.widget.TableRow
import androidx.appcompat.app.AppCompatActivity
import it.fnorg.bellapp.databinding.ActivityCreateMelodyBinding
import it.fnorg.bellapp.main_activity.MainActivity

class CreateMelodyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateMelodyBinding
    private lateinit var soundPool: SoundPool
    private val soundMap = mutableMapOf<String, Int>()
    private val handler = Handler(Looper.getMainLooper())
    private var numBells: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inizializza il binding
        binding = ActivityCreateMelodyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backArrow.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
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

        soundMap["C"] = soundPool.load(this, R.raw.c4, 1)
        soundMap["D"] = soundPool.load(this, R.raw.d4, 1)
        soundMap["E"] = soundPool.load(this, R.raw.e4, 1)
        soundMap["F"] = soundPool.load(this, R.raw.f4, 1)
        soundMap["G"] = soundPool.load(this, R.raw.g4, 1)

        numBells = intent.getIntExtra("NUM_BELLS", 0)
        generateBellButtons(numBells)
    }

    private fun generateBellButtons(numBells: Int) {
        val bellTable = binding.bellsTable
        bellTable.removeAllViews()

        val notes = listOf("C", "D", "E", "F", "G", "A", "B")
        val minColumns = 2
        val maxColumns = 4

        // Calcola il numero di colonne in base al numero di campane
        val numColumns = when {
            numBells <= 4 -> minColumns
            numBells <= 9 -> 3
            else -> maxColumns
        }

        // Calcola il numero di righe necessarie
        val numRows = (numBells + numColumns - 1) / numColumns

        // Calcola la dimensione dei pulsanti
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val buttonSize = (screenWidth / numColumns) - 2 * 8 // Larghezza dello schermo divisa per il numero di colonne, meno i margini

        // Limita la dimensione massima dei pulsanti per evitare che diventino troppo grandi
        val maxButtonSize = (screenWidth / minColumns) - 2 * 8
        val finalButtonSize = minOf(buttonSize, maxButtonSize)

        var bellsAdded = 0

        while (bellsAdded < numBells) {
            val tableRow = TableRow(this)
            tableRow.layoutParams = TableRow.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            tableRow.gravity = Gravity.CENTER // Centra i pulsanti verticalmente

            val remainingBells = numBells - bellsAdded
            val columnsInRow = if (remainingBells < numColumns) remainingBells else numColumns

            for (col in 0 until columnsInRow) {
                val bellButton = Button(this).apply {
                    text = notes[bellsAdded % notes.size] // Usa la nota corrispondente
                    tag = notes[bellsAdded % notes.size] // Usa la nota come tag
                    setBackgroundResource(R.drawable.ic_bell)
                    setTextColor(Color.WHITE)
                    textSize = 18f
                    setPadding(8, 8, 8, 8) // Aggiungi padding ai bottoni
                    setOnClickListener { onBellClick(it as Button) }
                }

                // Imposta i parametri del layout per garantire che i bottoni siano quadrati
                val params = TableRow.LayoutParams(
                    finalButtonSize, // Larghezza quadrata
                    finalButtonSize // Altezza quadrata
                ).apply {
                    setMargins(8, 8, 8, 8) // Aggiungi margini intorno ai bottoni
                }
                bellButton.layoutParams = params

                tableRow.addView(bellButton)
                bellsAdded++
            }

            // Aggiungi spazi vuoti per riempire e centrare i pulsanti in caso di riga incompleta
            for (col in columnsInRow until numColumns) {
                val emptySpace = Space(this)
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

        // Suona la nota per 500ms
        val soundId = soundMap[note] ?: return
        soundPool.play(soundId, 1f, 1f, 1, 0, 1f)

        // Avvia l'animazione
        startBellAnimation(button)

        // Ferma il suono dopo 500ms
        handler.postDelayed({
            soundPool.stop(soundId)
        }, 500)
    }

    private fun startBellAnimation(button: Button) {
        val scaleAnimation = ScaleAnimation(
            1f, 1.1f, 1f, 1.1f, // Scala da 1x a 1.1x
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 500 // Durata dell'animazione in millisecondi
            repeatMode = ScaleAnimation.REVERSE
            repeatCount = 1
        }

        button.startAnimation(scaleAnimation)
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
    }
}
