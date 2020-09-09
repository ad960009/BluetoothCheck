package kr.ad960009.bluetoothcheck

import android.accessibilityservice.AccessibilityService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import androidx.core.app.NotificationCompat

class ForegroundService : AccessibilityService() {
	val TAG = "ad960009"
	lateinit var receiver: PowerConnectReceiver

	override fun onAccessibilityEvent(p0: AccessibilityEvent?) {

	}

	override fun onInterrupt() {

	}

	override fun onCreate() {
		super.onCreate()

		SetForeground()
		Log.d(TAG, "onCreate")

		receiver = PowerConnectReceiver()
		val filter = IntentFilter()
		filter.addAction(Intent.ACTION_POWER_CONNECTED)
		filter.addAction(Intent.ACTION_POWER_DISCONNECTED)
		registerReceiver(receiver, filter)
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		if (intent != null) {
			val pkgName = intent.action
			if (pkgName == null)
				return START_STICKY
			if (pkgName.equals(""))
				return START_STICKY

			val intent = packageManager.getLaunchIntentForPackage(pkgName)
			if (intent != null) {
				startActivity(intent)
				Log.d("ad960009", "Start App $pkgName with ${intent.action}")
				for (category in intent.categories) {
					Log.d("ad960009", "\tcategory: $category")
				}
				ShowToast("App start: $pkgName")
			} else {
				Log.d("ad960009", "Can't start App $pkgName intent null")
				ShowToast("App start failed: $pkgName")
			}
		}
		return START_STICKY
	}

	override fun onDestroy() {
		Log.d(TAG, "onDestroy")
		unregisterReceiver(receiver)
		super.onDestroy()
	}

	fun SetForeground() {
		val channel = NotificationChannel(
			TAG,
			"Connection Receiver",
			NotificationManager.IMPORTANCE_DEFAULT
		)

		(getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
			channel
		)

		val builder = NotificationCompat.Builder(this, TAG)
		builder.setSmallIcon(R.drawable.placeholder)
		builder.setContentTitle(TAG)
		builder.setContentText("서비스 실행중")

		val indent = Intent(this, this::class.java)
		indent.action = exitAction
		val pendingIntent = PendingIntent.getForegroundService(this, 0, indent, 0)

		builder.setContentIntent(pendingIntent)
		startForeground(5, builder.build())
	}

	fun ShowToast(string: String) {
		Toast.makeText(this, string, Toast.LENGTH_SHORT).show()
	}

	companion object {
		const val startAction = "Start"
		const val exitAction = "Exit"
	}
}
