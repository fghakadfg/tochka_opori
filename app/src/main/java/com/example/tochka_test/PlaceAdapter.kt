import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import android.widget.BaseAdapter
import androidx.constraintlayout.widget.ConstraintLayout
import com.tochka_opory.tochka_test.R
import java.io.Console

data class ListItem(
    val iconResId: Int,
    val name: String,
    val about: String,
    val url: String,
    val soundResId: Int
)

interface OnPlaceClickListener {
    fun onPlaceClick()
}

class PlaceAdapter(private val context: Context, private val dataSource: List<ListItem>, private val placeClickListener: OnPlaceClickListener, private var isSwipeOrClick: Boolean) : BaseAdapter() {
    private var isSingleClick = false
    private val doubleClickInterval: Long = 300 // Интервал для двойного нажатия в миллисекундах

    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return dataSource.size
    }

    override fun getItem(position: Int): Any {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    private fun onSingleClick(soundResId: Int) {
        playSound(soundResId)
    }

    private fun onDoubleClick(url: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
        }
        ContextCompat.startActivity(context, intent, null)
    }

    fun updateSwipeFlag(isSwipeOrClick: Boolean) {
        this.isSwipeOrClick = isSwipeOrClick
        if (isSwipeOrClick) {
            mediaPlayer?.release() // Останавливаем звук, если произошел свайп
        }
        this.isSwipeOrClick = false
    }

    private fun playSound(soundResId: Int) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(context, soundResId)
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
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val rowView = convertView ?: inflater.inflate(R.layout.list_place, parent, false)

        val iconImagePlace = rowView.findViewById<ImageView>(R.id.place_icon)
        val namePlace = rowView.findViewById<TextView>(R.id.name_place)
        val aboutPlace = rowView.findViewById<TextView>(R.id.about_place)

        val listItem = getItem(position) as ListItem

        iconImagePlace.setImageResource(listItem.iconResId)
        namePlace.text = listItem.name
        aboutPlace.text = listItem.about

        // Проверяем флаг свайпа, если он установлен, то останавливаем звук
        if (isSwipeOrClick) {
            mediaPlayer?.release()
            this.isSwipeOrClick = false
        }

        rowView.setOnClickListener {
            if (isSingleClick) {
                isSingleClick = false
                handler.removeCallbacksAndMessages(null)
                onDoubleClick(listItem.url)
            } else {
                isSingleClick = true
                handler.postDelayed({
                    if (isSingleClick) {
                        isSingleClick = false
                        onSingleClick(listItem.soundResId)
                    }
                }, doubleClickInterval)
            }
            placeClickListener.onPlaceClick()
        }

        return rowView
    }
}
