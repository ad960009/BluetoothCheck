package kr.ad960009.bluetoothcheck

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Parcel
import android.os.Parcelable
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_timer.*
import java.util.*

class TimerActivity : AppCompatActivity() {
    var timer: CountDownTimer? = null;
    lateinit var timerTextView: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)

        timerTextView = findViewById<TextView>(R.id.textViewTimer)
    }

    override fun onResume() {
        super.onResume()
        timer = CountDown(10 * 60 * 1000, 10)
        timer?.start()
    }

    override fun onPause() {
        super.onPause()
        timer?.cancel()
        timer = null
    }

    fun UpdateText(millisUntilFinished: Long) {
        timerTextView.text =
            "${(millisUntilFinished / 1000)}.${((millisUntilFinished % 1000) / 10)}"
    }

    inner class CountDown(
        millisInFuture: Long,
        countDownInterval: Long
    ) : CountDownTimer(millisInFuture, countDownInterval) {
        override fun onTick(millisUntilFinished: Long) {
            this@TimerActivity.UpdateText(millisUntilFinished)
        }

        override fun onFinish() {
            this@TimerActivity.timer = null

        }
    }
}