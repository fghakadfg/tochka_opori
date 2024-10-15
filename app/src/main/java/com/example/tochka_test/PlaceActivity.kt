package com.tochka_opory.tochka_test

import ListItem
import OnPlaceClickListener
import PlaceAdapter
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.media.MediaPlayer
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Button
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tochka_test.FunctionActivity
import kotlin.math.abs

class PlaceActivity : AppCompatActivity(), GestureDetector.OnGestureListener, OnPlaceClickListener {
    private lateinit var function: Button
    private lateinit var main: Button
    private lateinit var place: Button

    private lateinit var placeAdapter: PlaceAdapter // адаптер теперь поле класса

    private var isSwipeOrClick = false
    private var mediaPlayer: MediaPlayer? = null

    private lateinit var gestureDetector: GestureDetectorCompat

    @SuppressLint("ClickableViewAccessibility", "SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_place)

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mediaPlayer?.release()

        function = findViewById(R.id.buttonFunction3)
        main = findViewById(R.id.buttonMain3)
        place = findViewById(R.id.buttonPlace3)

        // Установка слушателя на кнопку function
        function.setOnClickListener {
            goFuncView()
        }

        // Установка слушателя на кнопку main
        main.setOnClickListener {
            goMainView()
        }

        // Установка слушателя на кнопку stay_function_view
        place.setOnClickListener {
            playSound(R.raw.stay_place_raw)

            isSwipeOrClick = true // Устанавливаем флаг

            updateAdapterSwipeFlag()
        }

        val listView = findViewById<ListView>(R.id.placeListView)

        val listItems = listOf (
            ListItem(
                R.mipmap.ic_launcher_theatre_round,
                "Театр: “Внутреннее зрение”",
                "Коллектив театра «Внутреннее зрение» был создан в 1967 году. Он создавался для инвалидов по зрению и средствами драматического искусства должен был помочь им в социальной реабилитации.",
                "https://a-a-ah.ru/vnutrennee-zrenie",
                R.raw.theatre
            ),

            ListItem(
                R.mipmap.ic_launcher_spb_round,
                "Экскурсии с незрячим гидом “Осязаемый Петербург”",
                "Экскурсии-тренинги по городу с единственным в России незрячим гидом, квесты, занятия с детьми, создание доступной среды!",
                "https://vk.com/2feelspb",
                R.raw.spb
            ),

            ListItem(
                R.mipmap.ic_launcher_skuratov_round,
                "Skuratov Coffee на Малой Конюшенной",
                "Малая Конюшенная, 14\n" + "8-(996)-785-51-76",
                "https://skuratovcoffee.ru/spb",
                R.raw.skuratov1
            ),

            ListItem(
                R.mipmap.ic_launcher_skuratov_round,
                "Skuratov Coffee на Малой Посадской",
                "Малая Посадская, 4а\n" + "8-(996)-785-51-76",
                "https://skuratovcoffee.ru/spb",
                R.raw.skuratov1
            ),

            ListItem(
                R.mipmap.ic_launcher_beer_round,
                "Карл и Фридрих",
                "Южная дорога, 15\n" + "8-(812)-633-03-03",
                "https://k-f.ru/",
                R.raw.beer
            ),

            ListItem(
                R.mipmap.ic_launcher_hacho_puri_round,
                "Хачо и Пури на Алтайском",
                "Ул. Алтайская 12\n" + "8-(812)-610-25-25",
                "https://hachoipuri.ru/restoran/altajskaya",
                R.raw.hacho_puri1
            ),

            ListItem(
                R.mipmap.ic_launcher_hacho_puri_round,
                "Хачо и пури на Европейском",
                "Европейский пр., 8\n" + "8-(812)-329-08-08",
                "https://hachoipuri.ru/restoran/kudrovo",
                R.raw.hacho_puri1
            ),

            ListItem(
                R.mipmap.ic_launcher_hacho_puri_round,
                "Хачо и Пури на Левашовском",
                "Левашовский пр., 13А\n" + "8-(812)-655-55-77",
                "https://hachoipuri.ru/restoran/levashovskiy",
                R.raw.hacho_puri2
            ),

            ListItem(
                R.mipmap.ic_launcher_hacho_puri_round,
                "Хачо и Пури на Лиговском",
                "Лиговский пр., 29\n" + "8-(812)-901-11-09",
                "https://hachoipuri.ru/restoran/ligovskiy",
                R.raw.hacho_puri3
            ),

            ListItem(
                R.mipmap.ic_launcher_library_round,
                "Библиотека для слепых и слабовидящих",
                "Неофициальное название Библиотеки – «ТОЧКИ ЗРЕНИЯ»\n" + "Санкт-Петербургская государственная библиотека для слепых и слабовидящих – одна из крупнейших специальных библиотек России.",
                "https://www.gbs.spb.ru/ru/",
                R.raw.library
            ),

            ListItem(
                R.mipmap.ic_launcher_cucumbers_round,
                " Кафе “Огурцы”.",
                "Набережная реки Фонтанки, 96. \n" + "+7 (981) 909-71-09\n«Огурцы» – это первое инклюзивное кафе в городе. Находится в самом центре Петербурга, создалось и открылось командой проекта «Простые вещи».",
                "https://vk.com/ogurcicafe",
                R.raw.cucumbers
            ),
        )

        placeAdapter  = PlaceAdapter(this, listItems, this, isSwipeOrClick)
        listView.adapter = placeAdapter

        gestureDetector = GestureDetectorCompat(this, this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.place)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Воспроизведение звука, если есть переданный ресурс
        intent.getIntExtra("soundResId", 0).takeIf { it != 0 }?.let { soundResId ->
            playSound(soundResId)
        }
    }

    override fun onPause() {
        super.onPause()
        // Устанавливаем звуковой режим в тихий
        mediaPlayer?.release()
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
        if (isSwipeOrClick) mediaPlayer?.release()
    }

    private fun goFuncView() {
        isSwipeOrClick = true // Устанавливаем флаг

        updateAdapterSwipeFlag()

        mediaPlayer?.release()
        val intent = Intent(this@PlaceActivity, FunctionActivity::class.java)
        intent.putExtra("soundResId", R.raw.go_function_raw)
        startActivity(intent)
        overridePendingTransition(R.anim.swipe_in_right, R.anim.swipe_out_left)
    }

    private fun goMainView() {
        isSwipeOrClick = true // Устанавливаем флаг

        updateAdapterSwipeFlag()

        mediaPlayer?.release()
        val intent = Intent(this@PlaceActivity, MainActivity::class.java)
        intent.putExtra("soundResId", R.raw.go_main_view)
        startActivity(intent)
        overridePendingTransition(R.anim.swipe_in_left, R.anim.swipe_out_right)
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

    private fun updateAdapterSwipeFlag() {
        placeAdapter.updateSwipeFlag(isSwipeOrClick)
    }

    private fun onSwipeRight() { //в главное окно
        isSwipeOrClick = true // Устанавливаем флаг

        updateAdapterSwipeFlag()

        mediaPlayer?.release()
        val intent = Intent(this@PlaceActivity, MainActivity::class.java)
        intent.putExtra("soundResId", R.raw.go_main_view)
        startActivity(intent)
        overridePendingTransition(R.anim.swipe_in_left, R.anim.swipe_out_right)
    }

    private fun onSwipeLeft() { //в окно с функциями
        isSwipeOrClick = true // Устанавливаем флаг

        updateAdapterSwipeFlag()

        mediaPlayer?.release()
        val intent = Intent(this@PlaceActivity, FunctionActivity::class.java)
        intent.putExtra("soundResId", R.raw.go_function_raw)
        startActivity(intent)
        overridePendingTransition(R.anim.swipe_in_right, R.anim.swipe_out_left)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

    override fun onPlaceClick() {
        mediaPlayer?.release()
    }
}