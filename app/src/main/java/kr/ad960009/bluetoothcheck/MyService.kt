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
import androidx.preference.PreferenceManager
import java.util.concurrent.TimeUnit

class MyService : JobService() {
	lateinit var jobScheduler: JobScheduler
	override fun onStartJob(p0: JobParameters?): Boolean {
		jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

		val preference = PreferenceManager.getDefaultSharedPreferences(this)
		val selectedPackage1 =
			preference.getString(this.getString(R.string.packageSelect1), "") as String
		val selectedPackage2 =
			preference.getString(this.getString(R.string.packageSelect2), "") as String
		val selectedBluetooth =
			preference.getString(this.getString(R.string.bluetoothSelect), "") as String
		val id = p0?.jobId as Int;

		Log.d("ad960009", "jobService $id")
		when (id) {
			APP_START1 -> AppStart1(selectedPackage1)
			APP_START2 -> AppStart2(selectedPackage2)
			DELAYED_CHECK -> DelayedCheck(selectedBluetooth, selectedPackage1, selectedPackage2)
			//APP_STOP -> AppStop(selectedPackage1, selectedPackage2)
		}
		return true
	}

	override fun onStopJob(p0: JobParameters?): Boolean {
		return true
	}

	fun AppStart1(app: String) {
		Log.d("ad960009", "Job start App1 $app")
		if (app.isEmpty())
			return;

		/*val intent = packageManager.getLaunchIntentForPackage(app)
		if (intent != null) {
			startActivity(intent)
			Log.d("ad960009", "Start App1 $app with ${intent.action}")
			for(category in intent.categories)
			{
				Log.d("ad960009", "\tcategory: $category")
			}
		}
		else {
			Log.d("ad960009", "Can't start App1 $app intent null")
		}*/
		val serviceIntent = Intent(this, ForegroundService::class.java)
		serviceIntent.action = app;
		this.startService(serviceIntent)

		val componentName = ComponentName(this, MyService::class.java)
		val job = JobInfo.Builder(MyService.APP_START2, componentName)
			.setMinimumLatency(TimeUnit.SECONDS.toMillis(20))
			.setOverrideDeadline(TimeUnit.SECONDS.toMillis(40))
			.build()
		jobScheduler.schedule(job)
	}

	fun AppStart2(app: String) {
		if (app.isEmpty())
			return;

		Log.d("ad960009", "Job start App2 $app")
		/*val intent = packageManager.getLaunchIntentForPackage(app)
		if (intent != null) {
			startActivity(intent)
			Log.d("ad960009", "Start App2 $app with action: ${intent.action}")
			for(category in intent.categories)
			{
				Log.d("ad960009", "\tcategory: $category")
			}
		}
		else {
			Log.d("ad960009", "Can't start App2 $app intent null")
		}*/
		val serviceIntent = Intent(this, ForegroundService::class.java)
		serviceIntent.action = app;
		this.startService(serviceIntent)
	}

	/*fun CheckAppRunning(packageName: String): Boolean {
		val statManager= getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
		val current = System.currentTimeMillis()



		return true
	}*/

	fun DelayedCheck(bluetoothName: String, app1: String, app2: String) {
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
		const val APP_START1 = 1
		const val APP_START2 = 2
		const val DELAYED_CHECK = 3
		//const val APP_STOP = 4
	}
}
