package kr.ad960009.bluetoothcheck

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.AlertDialog
import android.app.AppOpsManager
import android.app.AppOpsManager.MODE_ALLOWED
import android.app.AppOpsManager.OPSTR_GET_USAGE_STATS
import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.preference.PreferenceManager


class MainActivity : AppCompatActivity() {

	lateinit var SettingValues: PreferenceValues

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		if (!checkAccessibilityPermissions()) {
			setAccessibilityPermissions();
		}

		SettingValues = PreferenceValues(this)

		val blueAppSelectTV1 = findViewById<TextView>(R.id.BlueSelect1)
		blueAppSelectTV1.setOnClickListener {
			val intent = Intent(this@MainActivity, ApplicationListActivity::class.java).also {
				startActivityForResult(it, 1)
			}
		}
		val blueAppSelectTV2 = findViewById<TextView>(R.id.BlueSelect2)
		blueAppSelectTV2.setOnClickListener {
			val intent = Intent(this@MainActivity, ApplicationListActivity::class.java).also {
				startActivityForResult(it, 2)
			}
		}
		val bluetoothSelectTV = findViewById<TextView>(R.id.bluetoothName)
		bluetoothSelectTV.setOnClickListener {
			val intent = Intent(this@MainActivity, BluetoothSelectActivity::class.java).also {
				startActivityForResult(it, 0)
			}
		}

		val powerAppSelectTV1 = findViewById<TextView>(R.id.PowerSelect1)
		powerAppSelectTV1.setOnClickListener {
			val intent = Intent(this@MainActivity, ApplicationListActivity::class.java).also {
				startActivityForResult(it, 3)
			}
		}
		val powerAppSelectTV2 = findViewById<TextView>(R.id.PowerSelect2)
		powerAppSelectTV2.setOnClickListener {
			val intent = Intent(this@MainActivity, ApplicationListActivity::class.java).also {
				startActivityForResult(it, 4)
			}
		}

		val runOnConnectedCheck = findViewById<CheckBox>(R.id.runOnConnected)
		val runOnDisconnectedCheck = findViewById<CheckBox>(R.id.runOnDisconnected)

		val runOnChargedCheck = findViewById<CheckBox>(R.id.runOnCharged)
		val runOnDischargedCheck = findViewById<CheckBox>(R.id.runOnDischarged)

		val screenOffCheck = findViewById<CheckBox>(R.id.screenOffAfterRun)

		val saveButton = findViewById<Button>(R.id.save_button)

		blueAppSelectTV1.text = SettingValues.BluetoothPackage1
		blueAppSelectTV2.text = SettingValues.BluetoothPackage2
		bluetoothSelectTV.text = SettingValues.BluetoothName

		powerAppSelectTV1.text = SettingValues.PowerPackage1
		powerAppSelectTV2.text = SettingValues.PowerPackage2

		runOnConnectedCheck.isChecked = SettingValues.RunOnConnected
		runOnDisconnectedCheck.isChecked = SettingValues.RunOnDisconnected

		runOnChargedCheck.isChecked = SettingValues.RunOnCharged
		runOnDischargedCheck.isChecked = SettingValues.RunOnDischarged

		screenOffCheck.isChecked = SettingValues.ScreenOff

		saveButton.setOnClickListener {
			SettingValues.BluetoothPackage1 = blueAppSelectTV1.text.toString()
			SettingValues.BluetoothPackage2 = blueAppSelectTV2.text.toString()
			SettingValues.BluetoothName = bluetoothSelectTV.text.toString()

			SettingValues.PowerPackage1 = powerAppSelectTV1.text.toString()
			SettingValues.PowerPackage2 = powerAppSelectTV2.text.toString()

			SettingValues.RunOnConnected = runOnConnectedCheck.isChecked
			SettingValues.RunOnDisconnected = runOnDisconnectedCheck.isChecked

			SettingValues.RunOnCharged = runOnChargedCheck.isChecked
			SettingValues.RunOnDischarged = runOnDischargedCheck.isChecked

			SettingValues.ScreenOff = screenOffCheck.isChecked

			SettingValues.Save()
		}

		val clearButton = findViewById<Button>(R.id.clear_button)
		clearButton.setOnClickListener {
			blueAppSelectTV1.text = ""
			blueAppSelectTV2.text = ""
			bluetoothSelectTV.text = ""

			powerAppSelectTV1.text = ""
			powerAppSelectTV2.text = ""

			runOnConnectedCheck.isChecked = false
			runOnDisconnectedCheck.isChecked = false

			runOnChargedCheck.isChecked = false
			runOnDischargedCheck.isChecked = false

			screenOffCheck.isChecked = false

			SettingValues.BluetoothPackage1 = blueAppSelectTV1.text.toString()
			SettingValues.BluetoothPackage2 = blueAppSelectTV2.text.toString()
			SettingValues.BluetoothName = bluetoothSelectTV.text.toString()

			SettingValues.PowerPackage1 = powerAppSelectTV1.text.toString()
			SettingValues.PowerPackage2 = powerAppSelectTV2.text.toString()

			SettingValues.RunOnConnected = runOnConnectedCheck.isChecked
			SettingValues.RunOnDisconnected = runOnDisconnectedCheck.isChecked

			SettingValues.RunOnCharged = runOnChargedCheck.isChecked
			SettingValues.RunOnDischarged = runOnDischargedCheck.isChecked

			SettingValues.ScreenOff = screenOffCheck.isChecked

			SettingValues.Save()
		}

		screenOffCheck.setOnClickListener {
			if (screenOffCheck.isChecked) {
				if (!checkAdminPermissions()) {
					val componentName = ComponentName(this, ShutdownConfigAdminReceiver::class.java)
					intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
					intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
					startActivityForResult(intent, 10)
				}
			}
		}

		val serviceIntent = Intent(this, ForegroundService::class.java)
		startService(serviceIntent)
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		if (resultCode == RESULT_OK) {
			if (requestCode == 1) {
				data?.getStringExtra(getString(R.string.packageSelect)).let {
					val tv = findViewById<TextView>(R.id.BlueSelect1)
					if (it == null)
						tv.text = ""
					else
						tv.text = it
				}
			} else if (requestCode == 2) {
				data?.getStringExtra(getString(R.string.packageSelect)).let {
					val tv = findViewById<TextView>(R.id.BlueSelect2)
					if (it == null)
						tv.text = ""
					else
						tv.text = it
				}
			} else if (requestCode == 0) {
				data?.getStringExtra(getString(R.string.bluetoothSelect)).let {
					val tv = findViewById<TextView>(R.id.bluetoothName)
					if (it == null)
						tv.text = ""
					else
						tv.text = it
				}
			} else if (requestCode == 3) {
				data?.getStringExtra(getString(R.string.packageSelect)).let {
					val tv = findViewById<TextView>(R.id.PowerSelect1)
					if (it == null)
						tv.text = ""
					else
						tv.text = it
				}
			} else if (requestCode == 4) {
				data?.getStringExtra(getString(R.string.packageSelect)).let {
					val tv = findViewById<TextView>(R.id.PowerSelect2)
					if (it == null)
						tv.text = ""
					else
						tv.text = it
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, data)
	}

	private fun checkForPermission(): Boolean {
		val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
		val mode = appOps.checkOpNoThrow(OPSTR_GET_USAGE_STATS, Process.myUid(), packageName)
		return mode == MODE_ALLOWED
	}

	fun checkAccessibilityPermissions(): Boolean {
		val accessibilityManager = getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager

		// getEnabledAccessibilityServiceList는 현재 접근성 권한을 가진 리스트를 가져오게 된다
		val list =
			accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.DEFAULT)
		for (i in list.indices) {
			val info = list[i]

			// 접근성 권한을 가진 앱의 패키지 네임과 패키지 네임이 같으면 현재앱이 접근성 권한을 가지고 있다고 판단함
			if (info.resolveInfo.serviceInfo.packageName == application.packageName) {
				return true
			}
		}
		return false
	}

	fun setAccessibilityPermissions() {
		val gsDialog: AlertDialog.Builder = AlertDialog.Builder(this)
		gsDialog.setTitle("접근성 권한 설정")
		gsDialog.setMessage("접근성 권한을 필요로 합니다")
		gsDialog.setPositiveButton(
			"확인",
			DialogInterface.OnClickListener { dialog, which -> // 설정화면으로 보내는 부분
				startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
				return@OnClickListener
			}).create().show()
	}

	fun checkAdminPermissions(): Boolean {
		var deviceManger = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager;
		val componentName = ComponentName(this, ShutdownConfigAdminReceiver::class.java)
		val hasPermission = deviceManger.isAdminActive(componentName)
		return hasPermission
	}
}

class ShutdownConfigAdminReceiver : DeviceAdminReceiver() {
	override fun onDisabled(context: Context, intent: Intent) {
		Log.d("ad960009", "admin disabled")
	}

	override fun onEnabled(context: Context, intent: Intent) {
		Log.d("ad960009", "admin enabled")
	}
}

class PreferenceValues(context: Context) {
	var RunOnConnected = false
	var RunOnDisconnected = false
	var BluetoothPackage1 = ""
	var BluetoothPackage2 = ""
	var BluetoothName = ""
	var RunOnCharged = false
	var RunOnDischarged = false
	var PowerPackage1 = ""
	var PowerPackage2 = ""
	var ScreenOff = false
	var appKilled = false

	val context: Context;

	fun getString(key: Int): String {
		return context.getString(key)
	}

	init {
		this.context = context;

		val preference = PreferenceManager.getDefaultSharedPreferences(context);

		RunOnConnected = preference.getBoolean(getString(R.string.runOnConnected), true)
		RunOnDisconnected = preference.getBoolean(getString(R.string.runOnDisconnected), true)
		RunOnCharged = preference.getBoolean(getString(R.string.runOnCharged), true)
		RunOnDischarged = preference.getBoolean(getString(R.string.runOnDischarged), true)

		BluetoothPackage1 =
			preference.getString(getString(R.string.bluePackageSelect1), "") as String
		BluetoothPackage2 =
			preference.getString(getString(R.string.bluePackageSelect2), "") as String
		BluetoothName = preference.getString(getString(R.string.bluetoothSelect), "") as String

		PowerPackage1 = preference.getString(getString(R.string.powerPackageSelect1), "") as String
		PowerPackage2 = preference.getString(getString(R.string.powerPackageSelect2), "") as String
		ScreenOff = preference.getBoolean(getString(R.string.screenOff), false)
		appKilled = preference.getBoolean(getString(R.string.appKilled), false)
	}

	fun Save() {
		val preference = PreferenceManager.getDefaultSharedPreferences(context);
		preference.edit {
			putBoolean(getString(R.string.runOnConnected), RunOnConnected)
			putBoolean(getString(R.string.runOnDisconnected), RunOnDisconnected)
			putBoolean(getString(R.string.runOnCharged), RunOnCharged)
			putBoolean(getString(R.string.runOnDischarged), RunOnDischarged)

			putString(getString(R.string.bluePackageSelect1), BluetoothPackage1)
			putString(getString(R.string.bluePackageSelect2), BluetoothPackage2)
			putString(getString(R.string.bluetoothSelect), BluetoothName)

			putString(getString(R.string.powerPackageSelect1), PowerPackage1)
			putString(getString(R.string.powerPackageSelect2), PowerPackage2)

			putBoolean(getString(R.string.screenOff), ScreenOff)
			putBoolean(getString(R.string.appKilled), appKilled)


			commit()
		}
	}
}