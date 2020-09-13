package kr.ad960009.bluetoothcheck

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import kr.ad960009.bluetoothcheck.MyService.Companion.longTerm
import kr.ad960009.bluetoothcheck.MyService.Companion.shortTerm
import java.util.concurrent.TimeUnit

class BluetoothReceiver : BroadcastReceiver() {
	override fun onReceive(context: Context, intent: Intent) {
		val SettingValues = PreferenceValues(context)

		if (Intent.ACTION_BOOT_COMPLETED.equals(intent.action)) {
			SettingValues.appKilled = false
			SettingValues.Save()

			val serviceIntent = Intent(context, ForegroundService::class.java)
			serviceIntent.action = "";
			context.startService(serviceIntent)
			return
		}

		// This method is called when the BroadcastReceiver is receiving an Intent broadcast.

		val action = intent.getAction() ?: return
		val device: BluetoothDevice? =
			intent?.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) ?: return

		val name = device?.name ?: return

		Log.d("ad960009", "bluetooth recv $action $name")

		if (SettingValues.BluetoothPackage1.isEmpty())
			return
		if (!name.equals(SettingValues.BluetoothName))
			return

		val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
		val componentName = ComponentName(context, MyService::class.java)

		when (action) {
			BluetoothDevice.ACTION_ACL_CONNECTED -> {
				ShowToast(context, "Recv Bluetooth Connected")

				jobScheduler.cancel(MyService.Event.POWER_DELAYED_CHECK.ordinal)
				jobScheduler.cancel(MyService.Event.BLUE_DELAYED_CHECK.ordinal)
				jobScheduler.cancel(MyService.Event.BLUE_APP_START1.ordinal)
				jobScheduler.cancel(MyService.Event.BLUE_APP_START2.ordinal)

				if (!SettingValues.RunOnConnected)
					return
				val job = JobInfo.Builder(MyService.Event.BLUE_APP_START1.ordinal, componentName)
					.setMinimumLatency(TimeUnit.SECONDS.toMillis(shortTerm))
					.setOverrideDeadline(TimeUnit.SECONDS.toMillis(shortTerm * 2))
					.build()
				jobScheduler.schedule(job)
			}
			BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
				ShowToast(context, "Recv Bluetooth Disconnected")

				jobScheduler.cancel(MyService.Event.BLUE_DELAYED_CHECK.ordinal)
				jobScheduler.cancel(MyService.Event.BLUE_APP_START1.ordinal)
				jobScheduler.cancel(MyService.Event.BLUE_APP_START2.ordinal)

				if (!SettingValues.RunOnDisconnected)
					return
				val job = JobInfo.Builder(MyService.Event.BLUE_DELAYED_CHECK.ordinal, componentName)
					.setMinimumLatency(TimeUnit.MINUTES.toMillis(longTerm))
					.setOverrideDeadline(TimeUnit.MINUTES.toMillis(longTerm * 2)).build()
				jobScheduler.schedule(job)
			}
		}
	}

	fun ShowToast(context: Context, string: String) {
		Toast.makeText(context, string, Toast.LENGTH_SHORT).show()
	}
}

class PowerConnectReceiver : BroadcastReceiver() {
	override fun onReceive(context: Context, intent: Intent) {

		val action = intent.getAction() ?: return

		val SettingValues = PreferenceValues(context)

		val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
		val componentName = ComponentName(context, MyService::class.java)

		when (action) {
			Intent.ACTION_POWER_CONNECTED -> {
				Log.d("ad960009", "Recv Power Connected")
				ShowToast(context, "Recv Power Connected")

				jobScheduler.cancel(MyService.Event.POWER_DELAYED_CHECK.ordinal)
				jobScheduler.cancel(MyService.Event.BLUE_DELAYED_CHECK.ordinal)
				jobScheduler.cancel(MyService.Event.POWER_APP_START1.ordinal)
				jobScheduler.cancel(MyService.Event.POWER_APP_START2.ordinal)

				if (!SettingValues.RunOnCharged)
					return
				val job = JobInfo.Builder(MyService.Event.POWER_APP_START1.ordinal, componentName)
					.setMinimumLatency(TimeUnit.SECONDS.toMillis(shortTerm))
					.setOverrideDeadline(TimeUnit.SECONDS.toMillis(shortTerm * 2))
					.build()
				jobScheduler.schedule(job)
			}
			Intent.ACTION_POWER_DISCONNECTED -> {
				Log.d("ad960009", "Recv Power Disconnected")
				ShowToast(context, "Recv Power Disconnected")

				jobScheduler.cancel(MyService.Event.POWER_DELAYED_CHECK.ordinal)
				jobScheduler.cancel(MyService.Event.POWER_APP_START1.ordinal)
				jobScheduler.cancel(MyService.Event.POWER_APP_START2.ordinal)

				if (!SettingValues.RunOnDischarged)
					return
				val job =
					JobInfo.Builder(MyService.Event.POWER_DELAYED_CHECK.ordinal, componentName)
						.setMinimumLatency(TimeUnit.MINUTES.toMillis(longTerm))
						.setOverrideDeadline(TimeUnit.MINUTES.toMillis(longTerm * 2))
						.build()
				jobScheduler.schedule(job)
			}
		}
	}

	fun ShowToast(context: Context, string: String) {
		Toast.makeText(context, string, Toast.LENGTH_SHORT).show()
	}
}