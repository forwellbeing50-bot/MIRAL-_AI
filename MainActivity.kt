package com.miral.ai

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private lateinit var statusText: TextView
    private lateinit var commandInput: EditText
    private lateinit var speechButton: Button
    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var ttsReady = false

    companion object {
        private const val AUDIO_PERMISSION_REQUEST = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        commandInput = findViewById(R.id.commandInput)
        speechButton = findViewById(R.id.micButton)

        tts = TextToSpeech(this, this)
        setupSpeechRecognizer()

        findViewById<Button>(R.id.sendButton).setOnClickListener {
            handleUserText(commandInput.text.toString())
        }
        speechButton.setOnClickListener {
            if (hasAudioPermission()) startListening() else requestAudioPermission()
        }
    }

    private fun setupSpeechRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            statusText.text = "اس فون پر speech recognition دستیاب نہیں ہے۔"
            speechButton.isEnabled = false
            return
        }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                statusText.text = "جی، میں سن رہی ہوں..."
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                statusText.text = "سمجھ رہی ہوں..."
            }
            override fun onError(error: Int) {
                statusText.text = "آواز واضح نہیں آئی۔ دوبارہ کوشش کریں یا لکھ کر بھیجیں۔"
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val spoken = matches?.firstOrNull().orEmpty()
                commandInput.setText(spoken)
                handleUserText(spoken)
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ur-PK")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ur-PK")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "MIRAL کو اپنا حکم بتائیں")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }
        try {
            speechRecognizer?.startListening(intent)
        } catch (_: Exception) {
            statusText.text = "Voice service شروع نہیں ہو سکی۔"
        }
    }

    private fun handleUserText(rawText: String) {
        val text = rawText.trim().lowercase(Locale.ROOT)
        if (text.isBlank()) {
            respond("جی، بتائیے کیا کرنا ہے؟")
            return
        }

        when {
            text.contains("settings") || text.contains("سیٹنگ") || text.contains("ترتیبات") -> {
                respond("جی، فون کی Settings کھول رہی ہوں۔")
                startActivity(Intent(Settings.ACTION_SETTINGS))
            }
            text.contains("hello") || text.contains("hi") ||
                text.contains("سلام") || text.contains("السلام") -> {
                respond("وعلیکم السلام! میں میرال ہوں۔ آج آپ کی کیا مدد کر سکتی ہوں؟")
            }
            text.contains("وقت") || text.contains("time") -> {
                val now = java.text.SimpleDateFormat("hh:mm a", Locale.getDefault()).format(java.util.Date())
                respond("ابھی وقت ہے $now")
            }
            text.contains("تم کون") || text.contains("who are you") -> {
                respond("میں میرال ہوں، آپ کی اردو سمجھنے والی AI assistant۔")
            }
            else -> {
                respond("میں نے آپ کی بات سن لی ہے۔ ابھی میرا ابتدائی ورژن ہے؛ اگلے مرحلے میں میرا AI brain اور live internet شامل کریں گے۔")
            }
        }
    }

    private fun respond(message: String) {
        statusText.text = message
        if (ttsReady) {
            tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, "miral_reply")
        }
    }

    override fun onInit(status: Int) {
        ttsReady = status == TextToSpeech.SUCCESS
        if (ttsReady) {
            val result = tts?.setLanguage(Locale("ur", "PK"))
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Device may not have an Urdu voice installed. Android's default voice is retained.
                tts?.setLanguage(Locale.getDefault())
            }
            tts?.setSpeechRate(0.96f)
        }
    }

    private fun hasAudioPermission(): Boolean =
        checkSelfPermission(Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED

    private fun requestAudioPermission() {
        requestPermissions(
            arrayOf(Manifest.permission.RECORD_AUDIO),
            AUDIO_PERMISSION_REQUEST
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == AUDIO_PERMISSION_REQUEST &&
            grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            startListening()
        } else if (requestCode == AUDIO_PERMISSION_REQUEST) {
            statusText.text = "Voice استعمال کرنے کے لیے microphone permission ضروری ہے۔"
        }
    }

    override fun onDestroy() {
        speechRecognizer?.destroy()
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
