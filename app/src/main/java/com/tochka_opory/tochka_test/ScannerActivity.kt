package com.tochka_opory.tochka_test

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import me.dm7.barcodescanner.zbar.Result
import me.dm7.barcodescanner.zbar.ZBarScannerView

class ScannerActivity : AppCompatActivity(), ZBarScannerView.ResultHandler {
    private lateinit var zbView: ZBarScannerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)
        zbView = findViewById(R.id.zbar_scanner_view) // Инициализация через findViewById
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
    override fun handleResult(result: Result?) {
        result?.let {
            // Получаем содержимое отсканированного штрих-кода
            val scanResult = it.contents
            // Выводим результат в лог
            println("Scan Result: $scanResult")
            // Вы можете добавить код для отображения результата пользователю или перехода к другой активности
            // Например, можно вывести результат в Toast:
            Toast.makeText(this, "Scan Result: $scanResult", Toast.LENGTH_LONG).show()

            // Если нужно, вы можете остановить камеру после получения результата
            zbView.stopCamera()
        }
    }

}