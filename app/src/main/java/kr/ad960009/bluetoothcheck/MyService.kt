package kr.ad960009.bluetoothcheck

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.concurrent.TimeUnit

class MyService : JobService() {
	lateinit var jobScheduler: JobScheduler
	override fun onStartJob(p0: JobParameters?): Boolean {
		jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

		val SettingValues = PreferenceValues(this)
		val id = p0?.jobId as Int;

		Log.d("ad960009", "jobService $id")
		when (id) {
			BLUE_APP_START1 -> BlueAppStart1(SettingValues.BluetoothPackage1)
			BLUE_APP_START2 -> BlueAppStart2(SettingValues.BluetoothPackage2)
			BLUE_DELAYED_CHECK -> BlueDelayedCheck(
				SettingValues.BluetoothName,
				SettingValues.BluetoothPackage1,
				SettingValues.BluetoothPackage2
			)

			POWER_APP_START1 -> PowerAppStart1(SettingValues.PowerPackage1)
			POWER_APP_START2 -> PowerAppStart2(SettingValues.PowerPackage2)
			POWER_DELAYED_CHECK -> PowerDelayedCheck(
				SettingValues.PowerPackage1,
				SettingValues.PowerPackage2
			)

			//APP_STOP -> AppStop(selectedPackage1, selectedPackage2)
		}
		return true
	}

	override fun onStopJob(p0: JobParameters?): Boolean {
		return true
	}

	fun BlueAppStart1(app: String) {
		Log.d("ad960009", "Job start App1 $app")
		if (app.isEmpty())
			return;

		val serviceIntent = Intent(this, ForegroundService::class.java)
		serviceIntent.action = app;
		this.startService(serviceIntent)

		val componentName = ComponentName(this, MyService::class.java)
		val job = JobInfo.Builder(MyService.BLUE_APP_START2, componentName)
			.setMinimumLatency(TimeUnit.SECONDS.toMillis(20))
			.setOverrideDeadline(TimeUnit.SECONDS.toMillis(40))
			.build()
		jobScheduler.schedule(job)
	}

	fun BlueAppStart2(app: String) {
		if (app.isEmpty())
			return;

		Log.d("ad960009", "Job start App2 $app")

		val serviceIntent = Intent(this, ForegroundService::class.java)
		serviceIntent.action = app;
		this.startService(serviceIntent)
	}

	fun BlueDelayedCheck(bluetoothName: String, app1: String, app2: String) {
		AppStop(app1, app2)
	}


	fun PowerAppStart1(app: String) {
		Log.d("ad960009", "Job start App1 $app")
		if (app.isEmpty())
			return;

		val serviceIntent = Intent(this, ForegroundService::class.java)
		serviceIntent.action = app;
		this.startService(serviceIntent)

		val componentName = ComponentName(this, MyService::class.java)
		val job = JobInfo.Builder(MyService.POWER_APP_START2, componentName)
			.setMinimumLatency(TimeUnit.SECONDS.toMillis(20))
			.setOverrideDeadline(TimeUnit.SECONDS.toMillis(40))
			.build()
		jobScheduler.schedule(job)
	}

	fun PowerAppStart2(app: String) {
		if (app.isEmpty())
			return;

		Log.d("ad960009", "Job start App2 $app")

		val serviceIntent = Intent(this, ForegroundService::class.java)
		serviceIntent.action = app;
		this.startService(serviceIntent)
	}

	fun PowerDelayedCheck(app1: String, app2: String) {
		AppStop(app1, app2)
	}

	fun AppStop(app1: String, app2: String) {
		Log.d("ad960009", "Job Kill Apps")
		val proc2 = Runtime.getRuntime().exec("su -c am force-stop $app2")
		proc2.waitFor()
		Log.d("ad960009", "Kill Apps $app2")
		val proc1 = Runtime.getRuntime().exec("su -c am force-stop $app1")
		proc1.waitFor()
		Log.d("ad960009", "Kill Apps $app1")
	}

	fun CheckBluetooth(bluetoothName: String): Boolean {
		val bondedList = BluetoothAdapter.getDefaultAdapter().getBondedDevices()
		for (bonded in bondedList) {
			val bondString = when (bonded.bondState) {
				BluetoothDevice.BOND_BONDED -> "bonded"
				BluetoothDevice.BOND_BONDING -> "bonding"
				BluetoothDevice.BOND_NONE -> "none"
				else -> "null"
			}
			Log.d("ad960009", "bond state: $bondString")

			if (bonded.name.equals(bluetoothName))
				return true
		}
		return false
	}

	companion object {
		const val BLUE_APP_START1 = 1
		const val BLUE_APP_START2 = 2
		const val BLUE_DELAYED_CHECK = 3
		const val POWER_APP_START1 = 4
		const val POWER_APP_START2 = 5
		const val POWER_DELAYED_CHECK = 5
		//const val APP_STOP = 4
	}
}
