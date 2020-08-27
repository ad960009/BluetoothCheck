package kr.ad960009.bluetoothcheck

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.AlertDialog
import android.app.AppOpsManager
import android.app.AppOpsManager.MODE_ALLOWED
import android.app.AppOpsManager.OPSTR_GET_USAGE_STATS
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.preference.PreferenceManager


class MainActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		if (!checkAccessibilityPermissions()) {
			setAccessibilityPermissions();
		}

		val preferences = PreferenceManager.getDefaultSharedPreferences(this);
		val selectedPackage1 =
			preferences.getString(getString(R.string.packageSelect1), "") as String
		val selectedPackage2 =
			preferences.getString(getString(R.string.packageSelect2), "") as String
		val selectedBluetooth =
			preferences.getString(getString(R.string.bluetoothSelect), "") as String

		val appSelectTV1 = findViewById<TextView>(R.id.Select1)
		appSelectTV1.setOnClickListener {
			val intent = Intent(this@MainActivity, ApplicationListActivity::class.java).also {
				startActivityForResult(it, 1)
			}
		}
		val appSelectTV2 = findViewById<TextView>(R.id.Select2)
		appSelectTV2.setOnClickListener {
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

		val saveButton = findViewById<Button>(R.id.save_button)
		if (!selectedPackage1.isEmpty()) {
			appSelectTV1.text = selectedPackage1
		}
		if (!selectedPackage2.isEmpty()) {
			appSelectTV2.text = selectedPackage2
		}
		if (!selectedBluetooth.isEmpty()) {
			bluetoothSelectTV.text = selectedBluetooth
		}

		saveButton.setOnClickListener {
			preferences.edit {
				var selectedPackage1 = ""
				var selectedPackage2 = ""
				var selectedBluetooth = ""
				if (appSelectTV1.text != null)
					selectedPackage1 = appSelectTV1.text.toString()
				if (appSelectTV2.text != null)
					selectedPackage2 = appSelectTV2.text.toString()
				if (bluetoothSelectTV.text != null)
					selectedBluetooth = bluetoothSelectTV.text.toString()
				putString(getString(R.string.packageSelect1), selectedPackage1)
				putString(getString(R.string.packageSelect2), selectedPackage2)
				putString(getString(R.string.bluetoothSelect), selectedBluetooth)
				commit()
			}
		}
		val clearButton = findViewById<Button>(R.id.clear_button)
		clearButton.setOnClickListener {
			preferences.edit {
				putString(getString(R.string.packageSelect1), "")
				putString(getString(R.string.packageSelect2), "")
				putString(getString(R.string.bluetoothSelect), "")
				appSelectTV1.text = ""
				appSelectTV2.text = ""
				bluetoothSelectTV.text = ""

				commit()
			}
		}

		val serviceIntent = Intent(this, ForegroundService::class.java)
		startService(serviceIntent)
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		if (resultCode == RESULT_OK) {
			if (requestCode == 1) {
				data?.getStringExtra(getString(R.string.packageSelect)).let {
					val tv = findViewById<TextView>(R.id.Select1)
					tv.text = it
				}
			}
			if (requestCode == 2) {
				data?.getStringExtra(getString(R.string.packageSelect)).let {
					val tv = findViewById<TextView>(R.id.Select2)
					tv.text = it
				}
			} else if (requestCode == 0) {
				data?.getStringExtra(getString(R.string.bluetoothSelect)).let {
					val tv = findViewById<TextView>(R.id.bluetoothName)
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
}