package kr.ad960009.bluetoothcheck

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.preference.PreferenceManager
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
		val preference = PreferenceManager.getDefaultSharedPreferences(context)
		val selectedPackage1 =
			preference.getString(context.getString(R.string.packageSelect1), "") as String
		val selectedPackage2 =
			preference.getString(context.getString(R.string.packageSelect2), "") as String
		val selectedBluetooth =
			preference.getString(context.getString(R.string.bluetoothSelect), "") as String

		val action = intent.getAction() ?: return
		val device: BluetoothDevice? =
			intent?.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) ?: return

		val name = device?.name ?: return

		Log.d("ad960009", "bluetooth recv $action $name")

		if (selectedPackage1.isEmpty())
			return
		if (!name.equals(selectedBluetooth))
			return

		val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
		val componentName = ComponentName(context, MyService::class.java)

		if (BluetoothDevice.ACTION_FOUND.equals(action)) {

		} else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
			// Job Add
			val job = JobInfo.Builder(MyService.APP_START1, componentName)
				.setMinimumLatency(TimeUnit.SECONDS.toMillis(shortTerm))
				.setOverrideDeadline(TimeUnit.SECONDS.toMillis(shortTerm * 2))
				.build()
			jobScheduler.cancelAll();
			jobScheduler.schedule(job)
		} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

		} else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
		} else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
			// Job Add
			val job = JobInfo.Builder(MyService.DELAYED_CHECK, componentName)
				.setMinimumLatency(TimeUnit.MINUTES.toMillis(longTerm))
				.setOverrideDeadline(TimeUnit.MINUTES.toMillis(longTerm * 2)).build()
			jobScheduler.cancelAll();
			jobScheduler.schedule(job)
		}

	}
}
