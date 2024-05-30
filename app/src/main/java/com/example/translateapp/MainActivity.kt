package com.example.translateapp

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.translateapp.databinding.ActivityMainBinding
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var languageArrayList: ArrayList<ModelLanguage>? = null
    private var sourceLanguageCode = "en"
    private var sourceLanguageTitle = "English"
    private var targetLanguageCode = "ru"
    private var targetLanguageTitle = "Russian"
    private lateinit var translatorOptions: TranslatorOptions
    private lateinit var translator: Translator
    private lateinit var progressDialog: ProgressDialog
    private lateinit var textToSpeech: TextToSpeech

    companion object {
        private const val TAG = "Main_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        loadAvailableLanguage()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.apply {
            binding.firstLanguageBtn.setOnClickListener {
                sourceLanguageChoose()
            }
            binding.secondLanguageBtn.setOnClickListener {
                targetLanguageChoose()
            }
            binding.translateBtn.setOnClickListener {
                validateData()
            }
            binding.firstSpeech.setOnClickListener {
                textToSpeech=TextToSpeech(this@MainActivity){
                    if (it==TextToSpeech.SUCCESS){
                        textToSpeech.setLanguage(Locale.UK)
                        textToSpeech.speak(sourceLanguageText,TextToSpeech.QUEUE_FLUSH,null,"")
                    }
                }

            }
            binding.secondSpeech.setOnClickListener {
                textToSpeech=TextToSpeech(this@MainActivity){
                    if (it==TextToSpeech.SUCCESS){
                        textToSpeech.setLanguage(Locale.US)
                        val speechtext=binding.translateText.text.toString()
                        textToSpeech.speak(speechtext,TextToSpeech.QUEUE_FLUSH,null,"")
                    }
                }

            }
            binding.backBtn.setOnClickListener { startActivity(Intent(this@MainActivity,SplashScreen::class.java)) }
        }
    }

    private var sourceLanguageText = ""
    private fun validateData() {
        sourceLanguageText = binding.originalText.text.toString().trim()
        if (sourceLanguageText.isEmpty()) {
            Toast.makeText(this, "Enter text to translate", Toast.LENGTH_SHORT).show()
        } else {
            progressDialog.setMessage("Processing language")
            progressDialog.show()
            translatorOptions = TranslatorOptions.Builder()
                .setSourceLanguage(sourceLanguageCode)
                .setTargetLanguage(targetLanguageCode)
                .build()
            translator = Translation.getClient(translatorOptions)

            val downloadConditions = DownloadConditions.Builder()
                .requireWifi()
                .build()
            translator.downloadModelIfNeeded(downloadConditions)
                .addOnSuccessListener {
                    progressDialog.setMessage("Translating...")
                    translator.translate(sourceLanguageText)
                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            binding.translateText.setText(it)
                        }
                        .addOnFailureListener {
                            progressDialog.dismiss()
                            Toast.makeText(
                                this,
                                "Failed translate ${it.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
                .addOnFailureListener {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Eror ${it.message}", Toast.LENGTH_SHORT).show()
                }

        }
    }


    private fun loadAvailableLanguage() {
        languageArrayList = ArrayList()
        val languageCodeList = TranslateLanguage.getAllLanguages()
        for (languageCode in languageCodeList) {
            val languageTitle = Locale(languageCode).displayName
            Log.d(TAG, "loadAvailableLanguage: languageCode :$languageCode")
            Log.d(TAG, "loadAvailableLanguage: languageTitle :$languageTitle")
            val modelLanguage = ModelLanguage(languageCode, languageTitle)
            languageArrayList!!.add(modelLanguage)
        }
    }

    private fun sourceLanguageChoose() {
        val popupMenu = PopupMenu(this, binding.firstLanguageBtn)
        for (i in languageArrayList!!.indices) {
            popupMenu.menu.add(Menu.NONE, i, i, languageArrayList!![i].languageTitle)
        }
        popupMenu.show()
        popupMenu.setOnMenuItemClickListener { menuItem ->
            val position = menuItem.itemId
            sourceLanguageCode = languageArrayList!![position].languageCode
            sourceLanguageTitle = languageArrayList!![position].languageTitle
            binding.firstLanguageBtn.text = sourceLanguageTitle
            binding.originalText.setHint("Enter $sourceLanguageTitle")
            Log.d(TAG, "sourceLanguageChoose: $sourceLanguageTitle")
            Log.d(TAG, "sourceLanguageChoose: $sourceLanguageCode")
            false
        }
    }

    private fun targetLanguageChoose() {
        val popupMenu = PopupMenu(this, binding.secondLanguageBtn)
        for (i in languageArrayList!!.indices) {
            popupMenu.menu.add(Menu.NONE, i, i, languageArrayList!![i].languageTitle)
        }
        popupMenu.show()
        popupMenu.setOnMenuItemClickListener { menuItem ->
            val position = menuItem.itemId
            targetLanguageCode = languageArrayList!![position].languageCode
            targetLanguageTitle = languageArrayList!![position].languageTitle
            binding.secondLanguageBtn.text = targetLanguageTitle
            Log.d(TAG, "targetLanguageChoose: $targetLanguageCode")
            Log.d(TAG, "targetLanguageChoose: $targetLanguageTitle")
            false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        textToSpeech.shutdown()
    }
}