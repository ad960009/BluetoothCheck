package kr.ad960009.bluetoothcheck

import android.app.admin.DevicePolicyManager
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
import android.widget.Toast
import java.util.concurrent.TimeUnit

class MyService : JobService() {
	lateinit var jobScheduler: JobScheduler
	override fun onStartJob(p0: JobParameters?): Boolean {
		jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

		val SettingValues = PreferenceValues(this)
		val id = p0?.jobId as Int;

		Log.d("ad960009", "jobService $id")
		when (id) {
			Event.BLUE_APP_START1.ordinal -> {
				if (SettingValues.appKilled) {
					BlueAppStart1(SettingValues.BluetoothPackage1)
				}
			}
			Event.BLUE_APP_START2.ordinal -> {
				BlueAppStart2(SettingValues.BluetoothPackage2)
				SettingValues.appKilled = false
				SettingValues.Save()
				if (SettingValues.ScreenOff) {
					val devicePolicyManager =
						getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
					devicePolicyManager.lockNow()
				}
			}
			Event.BLUE_DELAYED_CHECK.ordinal -> {
				SettingValues.appKilled = true
				SettingValues.Save()
				BlueDelayedCheck(
					SettingValues.BluetoothName,
					SettingValues.BluetoothPackage1,
					SettingValues.BluetoothPackage2
				)
			}

			Event.POWER_APP_START1.ordinal -> {
				if (SettingValues.appKilled) {
					PowerAppStart1(SettingValues.PowerPackage1)
				}
			}
			Event.POWER_APP_START2.ordinal -> {
				PowerAppStart2(SettingValues.PowerPackage2)
				SettingValues.appKilled = false
				SettingValues.Save()
				if (SettingValues.ScreenOff) {
					val devicePolicyManager =
						getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
					devicePolicyManager.lockNow()
				}
			}
			Event.POWER_DELAYED_CHECK.ordinal -> {
				SettingValues.appKilled = true
				SettingValues.Save()
				PowerDelayedCheck(
					SettingValues.PowerPackage1,
					SettingValues.PowerPackage2
				)
			}

			//APP_STOP -> AppStop(selectedPackage1, selectedPackage2)
		}
		return false
	}

	override fun onStopJob(p0: JobParameters?): Boolean {
		return false
	}

	fun BlueAppStart1(app: String) {
		if (app.isEmpty())
			return;
		ShowToast("Recv Bluetooth App Start1: $app")
		Log.d("ad960009", "Job start App1 $app")

		val serviceIntent = Intent(this, ForegroundService::class.java)
		serviceIntent.action = app;
		this.startService(serviceIntent)

		val componentName = ComponentName(this, MyService::class.java)
		val job = JobInfo.Builder(Event.BLUE_APP_START2.ordinal, componentName)
			.setMinimumLatency(TimeUnit.SECONDS.toMillis(20))
			.setOverrideDeadline(TimeUnit.SECONDS.toMillis(40))
			.build()
		jobScheduler.schedule(job)
	}

	fun BlueAppStart2(app: String) {
		if (app.isEmpty())
			return;

		ShowToast("Recv Bluetooth App Start2: $app")
		Log.d("ad960009", "Job start App2 $app")

		val serviceIntent = Intent(this, ForegroundService::class.java)
		serviceIntent.action = app;
		this.startService(serviceIntent)
	}

	fun BlueDelayedCheck(bluetoothName: String, app1: String, app2: String) {
		ShowToast("Recv Bluetooth delayed event")
		AppStop(app1, app2)
	}


	fun PowerAppStart1(app: String) {
		if (app.isEmpty())
			return;

		ShowToast("Recv Power App Start1: $app")
		Log.d("ad960009", "Job start App1 $app")

		val serviceIntent = Intent(this, ForegroundService::class.java)
		serviceIntent.action = app;
		this.startService(serviceIntent)

		val componentName = ComponentName(this, MyService::class.java)
		val job = JobInfo.Builder(Event.POWER_APP_START2.ordinal, componentName)
			.setMinimumLatency(TimeUnit.SECONDS.toMillis(20))
			.setOverrideDeadline(TimeUnit.SECONDS.toMillis(40))
			.build()
		jobScheduler.schedule(job)
	}

	fun PowerAppStart2(app: String) {
		if (app.isEmpty())
			return;

		ShowToast("Recv Power App Start2: $app")
		Log.d("ad960009", "Job start App2 $app")

		val serviceIntent = Intent(this, ForegroundService::class.java)
		serviceIntent.action = app;
		this.startService(serviceIntent)
	}

	fun PowerDelayedCheck(app1: String, app2: String) {
		ShowToast("Recv Power delayed event")
		AppStop(app1, app2)
	}

	fun AppStop(app1: String, app2: String) {
		Log.d("ad960009", "Job Kill Apps")
		if (!app2.isEmpty()) {
			ShowToast("Kill App: $app2")
			val proc2 = Runtime.getRuntime().exec("su -c am force-stop $app2")
			proc2.waitFor()
			Log.d("ad960009", "Kill Apps $app2")
		}
		if (!app1.isEmpty()) {
			ShowToast("Kill App: $app1")
			val proc1 = Runtime.getRuntime().exec("su -c am force-stop $app1")
			proc1.waitFor()
			Log.d("ad960009", "Kill Apps $app1")
		}
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

	fun ShowToast(string: String) {
		Toast.makeText(this, string, Toast.LENGTH_SHORT).show()
	}

	enum class Event {
		BLUE_APP_START1, BLUE_APP_START2, BLUE_DELAYED_CHECK, POWER_APP_START1, POWER_APP_START2, POWER_DELAYED_CHECK
	}
}
