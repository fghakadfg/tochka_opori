package com.tochka_opory.tochka_test

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tochka_test.FunctionActivity
import kotlin.math.abs

class MainActivity : AppCompatActivity(), GestureDetector.OnGestureListener {
    private lateinit var function: Button
    private lateinit var main: Button
    private lateinit var place: Button

    private lateinit var voice: Button
    private var isSingleClick = false
    private var isSwipe = false
    private val doubleClickInterval: Long = 300 // Интервал для двойного нажатия в миллисекундах

    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var gestureDetector: GestureDetectorCompat

    private lateinit var audioManager: AudioManager

    // Объявляем checkerFunc как статическую переменную в companion object
    companion object {
        var checkerFunc = true //для другого звук
        var checkerPlace = true //для другого звук
    }

    @SuppressLint("ClickableViewAccessibility", "SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mediaPlayer?.release()
        playSound(R.raw.welcome_raw)

        function = findViewById(R.id.buttonFunction1)
        main = findViewById(R.id.buttonMain1)
        place = findViewById(R.id.buttonPlace1)
        voice = findViewById(R.id.buttonInvisible)

        // Установка слушателя на кнопку function
        function.setOnClickListener {
            goFuncView()
        }

        // Установка слушателя на кнопку place
        place.setOnClickListener {
            goPlaceView()
        }

        // Установка слушателя на кнопку stay_main_view
        main.setOnClickListener {
            playSound(R.raw.stay_main_view)
        }

        voice.setOnClickListener {
            if (isSingleClick) {
                isSingleClick = false
                handler.removeCallbacksAndMessages(null)
                onDoubleClick()
            } else {
                isSingleClick = true
                handler.postDelayed({
                    if (isSingleClick) {
                        isSingleClick = false
                        onSingleClick()
                    }
                }, doubleClickInterval)
            }
        }

        gestureDetector = GestureDetectorCompat(this, this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Воспроизведение звука, если есть переданный ресурс
        intent.getIntExtra("soundResId", 0).takeIf { it != 0 }?.let { soundResId ->
            playSound(soundResId)
        }

        // Инициализируем AudioManager
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    override fun onPause() {
        super.onPause()
        // Устанавливаем звуковой режим в тихий
        mediaPlayer?.release()
    }

    private fun onSingleClick() {
        mediaPlayer?.release()
        playSound(R.raw.instruction_raw)
    }

    private fun onDoubleClick() {
        mediaPlayer?.release()
        playSoundSequentially(R.raw.about_raw, R.raw.instruction_raw)
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

    private fun playSoundSequentially(firstSoundResId: Int, secondSoundResId: Int) {
        mediaPlayer = MediaPlayer.create(this, firstSoundResId)
        mediaPlayer?.setOnCompletionListener {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(this, secondSoundResId)
            mediaPlayer?.apply {
                playbackParams = playbackParams.setSpeed(2.0f)
                start()
            }
        }
        mediaPlayer?.apply {
            playbackParams = playbackParams.setSpeed(2.0f)
            start()
        }
        if (isSwipe) mediaPlayer?.release()
    }


    private fun goFuncView() {
        mediaPlayer?.release()
        val intent = Intent(this@MainActivity, FunctionActivity::class.java)

        // Проверяем значение checkerFunc
        if (checkerFunc) {
            intent.putExtra("soundResId", R.raw.go_function_raw)
            checkerFunc = false // Меняем на false
        } else {
            intent.putExtra("soundResId", R.raw.go_function_raw_2)
        }

        startActivity(intent)
        overridePendingTransition(R.anim.swipe_in_left, R.anim.swipe_out_right)
    }

    private fun goPlaceView() {
        mediaPlayer?.release()
        val intent = Intent(this@MainActivity, PlaceActivity::class.java)

        if (checkerPlace) {
            intent.putExtra("soundResId", R.raw.go_place_raw)
            checkerPlace = false // Меняем на false
        } else {
            intent.putExtra("soundResId", R.raw.go_place_raw_2)
        }

        startActivity(intent)
        overridePendingTransition(R.anim.swipe_in_right, R.anim.swipe_out_left)
    }

    override fun onDown(p0: MotionEvent): Boolean = true //просто скип функций

    override fun onShowPress(p0: MotionEvent) {} //просто скип функций

    override fun onSingleTapUp(p0: MotionEvent): Boolean = true //просто скип функций

    override fun onScroll(p0: MotionEvent?, p1: MotionEvent, p2: Float, p3: Float): Boolean = true //просто скип функций

    override fun onLongPress(p0: MotionEvent) {} //просто скип функций

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

    private fun onSwipeRight() { //в окно с функциями местами
        mediaPlayer?.release()
        val intent = Intent(this@MainActivity, FunctionActivity::class.java)

        // Проверяем значение checkerFunc
        if (checkerFunc) {
            intent.putExtra("soundResId", R.raw.go_function_raw)
            checkerFunc = false // Меняем на false
        } else {
            intent.putExtra("soundResId", R.raw.go_function_raw_2)
        }

        startActivity(intent)
        overridePendingTransition(R.anim.swipe_in_left, R.anim.swipe_out_right)
        isSwipe = true
    }

    private fun onSwipeLeft() { //в окно с местами
        mediaPlayer?.release()
        val intent = Intent(this@MainActivity, PlaceActivity::class.java)

        if (checkerPlace) {
            intent.putExtra("soundResId", R.raw.go_place_raw)
            checkerPlace = false // Меняем на false
        } else {
            intent.putExtra("soundResId", R.raw.go_place_raw_2)
        }

        startActivity(intent)
        overridePendingTransition(R.anim.swipe_in_right, R.anim.swipe_out_left)
        isSwipe = true
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }
}
