package com.tochka_opory.tochka_test

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tochka_test.FunctionActivity
import me.dm7.barcodescanner.zbar.Result
import me.dm7.barcodescanner.zbar.ZBarScannerView
import java.util.Locale

class ScannerActivity : AppCompatActivity(), ZBarScannerView.ResultHandler, TextToSpeech.OnInitListener {
    private lateinit var zbView: ZBarScannerView
    private lateinit var tts: TextToSpeech
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)
        zbView = findViewById(R.id.zbar_scanner_view) // Инициализация через findViewById
        tts = TextToSpeech(this, this)
    }

    override fun onResume() {

        super.onResume()

        zbView.setResultHandler(this)
        zbView.startCamera()
    }

    override fun onPause() {
        super.onPause()
        zbView.stopCamera()
    }

    private fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    override fun handleResult(result: Result?) {
        result?.let {
            // Получаем содержимое отсканированного штрих-кода
            val scanResult = it.contents
            // Выводим результат в лог
            println("Scan Result: $scanResult")
            // Вы можете добавить код для отображения результата пользователю или перехода к другой активности
            // Например, можно вывести результат в Toast:
            // Toast.makeText(this, "Scan Result: $scanResult", Toast.LENGTH_LONG).show() КОММИТ
            speak("Перед вами $scanResult")
            // Если нужно, вы можете остановить камеру после получения результата

            zbView.stopCamera()

            val intent = Intent(this, FunctionActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Устанавливаем язык озвучки
            val result = tts.setLanguage(Locale("ru")) // Можно заменить на Locale("ru") для русского
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Язык не поддерживается")
            }
        } else {
            Log.e("TTS", "Ошибка инициализации TTS")
        }
    }

}