package kr.ad960009.bluetoothcheck

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.concurrent.TimeUnit

class BluetoothReceiver : BroadcastReceiver() {
	companion object {
		const val longTerm: Long = 10
		const val shortTerm: Long = 10
	}

	override fun onReceive(context: Context, intent: Intent) {
		if (Intent.ACTION_BOOT_COMPLETED.equals(intent.action)) {
			val serviceIntent = Intent(context, ForegroundService::class.java)
			serviceIntent.action = "";
			context.startService(serviceIntent)
			return
		}

		// This method is called when the BroadcastReceiver is receiving an Intent broadcast.

		val SettingValues = PreferenceValues(context)

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
				if (!SettingValues.RunOnConnected)
					return
				val job = JobInfo.Builder(MyService.BLUE_APP_START1, componentName)
					.setMinimumLatency(TimeUnit.SECONDS.toMillis(shortTerm))
					.setOverrideDeadline(TimeUnit.SECONDS.toMillis(shortTerm * 2))
					.build()
				jobScheduler.cancel(MyService.BLUE_DELAYED_CHECK)
				jobScheduler.cancel(MyService.BLUE_APP_START1)
				jobScheduler.cancel(MyService.BLUE_APP_START2)
				jobScheduler.schedule(job)
			}
			BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
				if (!SettingValues.RunOnDisconnected)
					return
				val job = JobInfo.Builder(MyService.BLUE_DELAYED_CHECK, componentName)
					.setMinimumLatency(TimeUnit.MINUTES.toMillis(longTerm))
					.setOverrideDeadline(TimeUnit.MINUTES.toMillis(longTerm * 2)).build()
				jobScheduler.cancel(MyService.BLUE_DELAYED_CHECK)
				jobScheduler.cancel(MyService.BLUE_APP_START1)
				jobScheduler.cancel(MyService.BLUE_APP_START2)
				jobScheduler.schedule(job)
			}
		}
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
				if (!SettingValues.RunOnCharged)
					return
				val job = JobInfo.Builder(MyService.POWER_APP_START1, componentName)
					.setMinimumLatency(TimeUnit.SECONDS.toMillis(BluetoothReceiver.shortTerm))
					.setOverrideDeadline(TimeUnit.SECONDS.toMillis(BluetoothReceiver.shortTerm * 2))
					.build()
				jobScheduler.cancel(MyService.POWER_DELAYED_CHECK)
				jobScheduler.cancel(MyService.POWER_APP_START1)
				jobScheduler.cancel(MyService.POWER_APP_START2)
				jobScheduler.schedule(job)
			}
			Intent.ACTION_POWER_DISCONNECTED -> {
				if (!SettingValues.RunOnDischarged)
					return
				val job = JobInfo.Builder(MyService.POWER_DELAYED_CHECK, componentName)
					.setMinimumLatency(TimeUnit.MINUTES.toMillis(BluetoothReceiver.longTerm))
					.setOverrideDeadline(TimeUnit.MINUTES.toMillis(BluetoothReceiver.longTerm * 2))
					.build()
				jobScheduler.cancel(MyService.POWER_DELAYED_CHECK)
				jobScheduler.cancel(MyService.POWER_APP_START1)
				jobScheduler.cancel(MyService.POWER_APP_START2)
				jobScheduler.schedule(job)
			}
		}
	}
}