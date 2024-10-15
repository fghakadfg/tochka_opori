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

            if (scanResult.toString() == "http://dobry.ru/fermery/yabloko") {
                speak("Перед вами яблочный сок Добрый, Состав сока: Яблочный сок, яблочное пюре, сахар, регулятор кислотности - лимонная кислота, вода")
                zbView.stopCamera()

                val intent = Intent(this, FunctionActivity::class.java)
                startActivity(intent)
            } else {
                if (scanResult.toString() == "https://krupa-prosto.ru/products/grechka-dlya-garnira/?utm_source=pack-qr&utm_medium=organic&utm_campaign=prosto&utm_content=grechka-dlya-garnira") {
                    speak(
                        "Название:  Гречка для гарнира  \"PROSTO\" 400 грамм.  \n" +
                                "Состав: Высший сорт крупы гречневой. Продукт может содержать следы глютена. \n" +
                                "БЖУ: На 100 г крупы - жиры 3,5 грамма, Белки 13 грамм, Углеводы 64 грамма. 1420 калорий. \n" +
                                "Срок годности: 12 месяцев от даты производства \n" +
                                "Производитель: Россия, Краснодарский край, Красноармейский район. ООО \"Компания \"Ангстрем Трейдинг\"."
                    )
                    zbView.stopCamera()

                    val intent = Intent(this, FunctionActivity::class.java)
                    startActivity(intent)
                } else {
                    if (scanResult.toString() == "http://www.makfa.ru/catalog/makaronnaya-produktsiya/?utm_source=qrcode&utm_medium=pack&utm_campaign=mimakfa&utm_content=roz") {
                        speak(
                            "Название: Макароны \"MAKFA\" Улитки\n" +
                                    "Состав: Мука из твёрдой пшеницы для макаронных изделий высшего сорта, вода питьевая. \n" +
                                    "БЖУ: Белки 12 грамм, Жиры 1,3 грамма, Углеводы 70,5 грамм. \n" +
                                    "Срок годности: от 04.09.2024 до 04.09.2026\n" +
                                    "Производитель: АО \"МАКФА\" Россия г. Москва, переулок Вспольный."
                        )
                        zbView.stopCamera()

                        val intent = Intent(this, FunctionActivity::class.java)
                        startActivity(intent)
                    } else {
                        if (scanResult.toString() == "https://martin.su/?bc=4607012351951&suf=02") {
                            speak(
                                "Название: Семечки отборные от Мартина 100 грамм\n" +
                                        "Состав: Семечки подсолнечные, соль. \n" +
                                        "Срок годности: от 27.09.2024 до 12.09.2025\n" +
                                        "Производитель: ООО \"МАРТИН БЕЛ\""
                            )
                            zbView.stopCamera()

                            val intent = Intent(this, FunctionActivity::class.java)
                            startActivity(intent)
                        } else {
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
                }
            }
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