package com.example.tochka_test

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap

import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.Locale
import kotlin.math.abs


class FunctionActivity : AppCompatActivity(), GestureDetector.OnGestureListener, TextToSpeech.OnInitListener {
    private lateinit var function: Button
    private lateinit var main: Button
    private lateinit var place: Button
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var tts: TextToSpeech
    private var isSwipe = false

    private lateinit var gestureDetector: GestureDetectorCompat

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)
        enableEdgeToEdge()
        setContentView(R.layout.activity_function)

        mediaPlayer?.release()

        function = findViewById(R.id.buttonFunction2)
        main = findViewById(R.id.buttonMain2)
        place = findViewById(R.id.buttonPlace2)

        // Установка слушателя на кнопку main
        main.setOnClickListener {
            goMainView()
        }

        // Установка слушателя на кнопку place
        place.setOnClickListener {
            goPlaceView()
        }

        // Установка слушателя на кнопку stay_function_view
        function.setOnClickListener {
            playSound(R.raw.stay_function_raw)
        }

        gestureDetector = GestureDetectorCompat(this, this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.function)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Воспроизведение звука, если есть переданный ресурс
        intent.getIntExtra("soundResId", 0).takeIf { it != 0 }?.let { soundResId ->
            playSound(soundResId)
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

    // Функция для озвучивания текста
    private fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    override fun onDestroy() {
        // Освобождаем ресурсы TTS при завершении активности
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            // Преобразование URI в Bitmap
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // Вызов функции для распознавания текста
            textServerSend(bitmap)
        } ?: run {
            Toast.makeText(this, "Не удалось выбрать изображение", Toast.LENGTH_SHORT).show()
        }
    }
    private fun selectImage() {
        // Открытие галереи для выбора изображения
        pickImageLauncher.launch("image/*")
    }
    private fun playSound(soundResId: Int) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(this, soundResId)
        mediaPlayer?.apply {
            if (isPlaying) {
                playbackParams = playbackParams.setSpeed(2.0f)
            } else {
                setOnPreparedListener {
                    it.playbackParams = it.playbackParams.setSpeed(2.0f)
                    it.start()
                }
            }
            start()
        }
        if (isSwipe) mediaPlayer?.release()
    }

    private fun goMainView() {
        mediaPlayer?.release()
        val intent = Intent(this@FunctionActivity, MainActivity::class.java)
        intent.putExtra("soundResId", R.raw.go_main_view)
        startActivity(intent)
        overridePendingTransition(R.anim.swipe_in_right, R.anim.swipe_out_left)
    }
    public fun textRecog(view: View) {
        selectImage()
    }

    public fun textServerSend(bitmap: Bitmap) {
        Log.d("textRecog", "Function called")

        // Преобразование Bitmap в массив байтов
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        Log.d("textRecog", "Bitmap converted to byte array")

        // Создание OkHttp клиента
        val client = OkHttpClient()

        // Создание тела запроса
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", "image.jpg",
                RequestBody.create("image/jpeg".toMediaTypeOrNull(), byteArray))
            .build()

        Log.d("textRecog", "Request body created")

        // Создание запроса
        val request = Request.Builder()
            .url("http://192.168.1.69:5000/process-image") // Замените на реальный IP и порт
            .post(requestBody)
            .build()

        // Отправка запроса
        Thread {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseText = response.body?.string()

                    // Обработка JSON-ответа
                    runOnUiThread {
                        if (responseText != null) {
                            // Проверка на корректность JSON
                            try {
                                val jsonObject = JSONObject(responseText)
                                val resultText = jsonObject.getString("result")
                                speak(resultText)  // Передаем строку в функцию для озвучивания
                                Log.d("textRecog", "Response result: $resultText")
                            } catch (e: JSONException) {
                                Log.e("textRecog", "Failed to parse JSON", e)
                            }
                        }
                    }
                } else {
                    Log.e("textRecog", "Unexpected response code: ${response.code}")
                }
            } catch (e: IOException) {
                Log.e("textRecog", "Request failed", e)
            }
        }.start()
    }

    // Метод для выбора изображения
    private val pickImageLauncherForObjectDetection = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            // Преобразование URI в Bitmap
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // Вызов функции для распознавания объектов
            objectDetectionServerSend(bitmap)
        } ?: run {
            Toast.makeText(this, "Не удалось выбрать изображение", Toast.LENGTH_SHORT).show()
        }
    }

    // Функция для выбора изображения для распознавания объектов
    private fun selectImageForObjectDetection() {
        // Открытие галереи для выбора изображения
        pickImageLauncherForObjectDetection.launch("image/*")
    }

    // Новый метод для распознавания объектов на сервере
    public fun objectDetection(view: View) {
        selectImageForObjectDetection()
    }

    public fun objectDetectionServerSend(bitmap: Bitmap) {
        Log.d("objectDetection", "Function called")

        // Преобразование Bitmap в массив байтов
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        Log.d("objectDetection", "Bitmap converted to byte array")

        // Создание OkHttp клиента
        val client = OkHttpClient()

        // Создание тела запроса
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", "image.jpg",
                RequestBody.create("image/jpeg".toMediaTypeOrNull(), byteArray))
            .build()

        Log.d("objectDetection", "Request body created")

        // Создание запроса для распознавания объектов
        val request = Request.Builder()
            .url("http://192.168.1.69:8000/process_result") // Замените на реальный endpoint для детекции объектов
            .post(requestBody)
            .build()

        // Отправка запроса
        Thread {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseText = response.body?.string()

                    // Обработка JSON-ответа
                    runOnUiThread {
                        if (responseText != null) {
                            // Проверка на корректность JSON
                            try {
                                val jsonObject = JSONObject(responseText)
                                val detectedObjects = jsonObject.getJSONArray("objects")

                                // Пример обработки массива объектов
                                val objectsList = mutableListOf<String>()
                                for (i in 0 until detectedObjects.length()) {
                                    val objectName = detectedObjects.getString(i) // Получаем строку напрямую
                                    objectsList.add(objectName) // Добавляем строку в список
                                }
                                val resultText = objectsList.joinToString(", ")

                                // Озвучиваем результат
                                speak(resultText)  // Передаем строку в функцию для озвучивания
                                Log.d("objectDetection", "Detected objects: $objectsList")
                                Toast.makeText(this@FunctionActivity, "Объекты: $objectsList", Toast.LENGTH_LONG).show()
                            } catch (e: JSONException) {
                                Log.e("objectDetection", "Failed to parse JSON", e)
                            }
                        }
                    }
                } else {
                    Log.e("objectDetection", "Unexpected response code: ${response.code}")
                }
            } catch (e: IOException) {
                Log.e("objectDetection", "Request failed", e)
            }
        }.start()
    }


    private fun goPlaceView() {
        mediaPlayer?.release()
        val intent = Intent(this@FunctionActivity, PlaceActivity::class.java)
        intent.putExtra("soundResId", R.raw.go_place_raw)
        startActivity(intent)
        overridePendingTransition(R.anim.swipe_in_left, R.anim.swipe_out_right)
    }

    override fun onDown(p0: MotionEvent): Boolean = true // просто скип функций

    override fun onShowPress(p0: MotionEvent) {} // просто скип функций

    override fun onSingleTapUp(p0: MotionEvent): Boolean = true // просто скип функций

    override fun onScroll(p0: MotionEvent?, p1: MotionEvent, p2: Float, p3: Float): Boolean = true // просто скип функций

    override fun onLongPress(p0: MotionEvent) {} // просто скип функций

    override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        if (e1 != null && e2 != null) {
            val deltaX = e2.x - e1.x
            val deltaY = e2.y - e1.y
            if (abs(deltaX) > abs(deltaY)) {
                if (abs(deltaX) > 100 && abs(velocityX) > 100) {
                    if (deltaX > 0) {
                        onSwipeRight()
                    } else {
                        onSwipeLeft()
                    }
                    return true
                }
            }
        }
        return false
    }

    private fun onSwipeRight() { // в окно с местами
        mediaPlayer?.release()
        val intent = Intent(this@FunctionActivity, PlaceActivity::class.java)
        intent.putExtra("soundResId", R.raw.go_place_raw)
        startActivity(intent)
        overridePendingTransition(R.anim.swipe_in_left, R.anim.swipe_out_right)
        isSwipe = true
    }

    private fun onSwipeLeft() { // в главное окно
        mediaPlayer?.release()
        val intent = Intent(this@FunctionActivity, MainActivity::class.java)
        intent.putExtra("soundResId", R.raw.go_main_view)
        startActivity(intent)
        overridePendingTransition(R.anim.swipe_in_right, R.anim.swipe_out_left)
        isSwipe = true
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }
}