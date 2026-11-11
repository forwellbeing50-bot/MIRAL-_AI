package com.miral.ai

import com.miral.ai.R
import android.app.Activity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Date

class MainActivity : Activity(), TextToSpeech.OnInitListener {

    private lateinit var statusText: TextView
    private lateinit var commandInput: EditText
    private lateinit var sendButton: Button
    private lateinit var micButton: Button

    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        commandInput = findViewById(R.id.commandInput)
        sendButton = findViewById(R.id.sendButton)
        micButton = findViewById(R.id.micButton)

        tts = TextToSpeech(this, this)

        sendButton.setOnClickListener {
            val message = commandInput.text.toString().trim()

            if (message.isNotEmpty()) {
                respond(message)
                commandInput.text.clear()
            } else {
                Toast.makeText(
                    this,
                    "Pehle message likhein",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        micButton.setOnClickListener {
            startVoiceInput()
        }
    }

    private fun respond(message: String) {

        val input = message.lowercase(Locale.ROOT)

        val reply = when {

            input.contains("salam") ||
            input.contains("السلام") -> {
                "Wa Alaikum Assalam! Main MIRAL hoon. Batayein kya madad kar sakti hoon?"
            }

            input.contains("hello") ||
            input.contains("hi") -> {
                "Hello! Aaj kya baat karni hai?"
            }

            input.contains("name") ||
            input.contains("naam") -> {
                "Mera naam MIRAL hai. Main aapki Urdu assistant hoon."
            }

            input.contains("time") ||
            input.contains("waqt") -> {
                val time = SimpleDateFormat(
                    "hh:mm a",
                    Locale.getDefault()
                ).format(Date())

                "Abhi waqt hai $time"
            }

            input.contains("how are you") ||
            input.contains("kaisi ho") -> {
                "Main bilkul ready hoon! Aap sunayein kaise hain?"
            }

            input.contains("bye") ||
            input.contains("allah hafiz") -> {
                "Allah Hafiz! Phir baat hogi."
            }

            else -> {
                "Aapki baat samajh rahi hoon. Filhal main basic preview mode mein hoon. Advanced AI connection agle stage mein add karenge."
            }
        }

        statusText.text = reply
        speak(reply)
    }

    private fun startVoiceInput() {

        try {

            val intent = Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH
            )

            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )

            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                "ur-PK"
            )

            intent.putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                "MIRAL se baat karein"
            )

            startActivityForResult(intent, 100)

        } catch (e: Exception) {

            Toast.makeText(
                this,
                "Voice input available nahi hai",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (requestCode == 100 && resultCode == RESULT_OK) {

            val results = data?.getStringArrayListExtra(
                RecognizerIntent.EXTRA_RESULTS
            )

            val spokenText = results?.firstOrNull()

            if (!spokenText.isNullOrEmpty()) {

                commandInput.setText(spokenText)

                respond(spokenText)

                commandInput.text.clear()
            }
        }
    }

    private fun speak(text: String) {

        tts?.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "MIRAL"
        )
    }

    override fun onInit(status: Int) {

        if (status == TextToSpeech.SUCCESS) {

            val result = tts?.setLanguage(
                Locale("ur", "PK")
            )

            if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED) {

                statusText.text =
                    "Urdu voice supported nahi hai. Phone ki TTS settings check karein."
            }
        }
    }

    override fun onDestroy() {

        tts?.stop()
        tts?.shutdown()

        super.onDestroy()
    }
}
