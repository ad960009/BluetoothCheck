package kr.ad960009.bluetoothcheck

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class BluetoothSelectActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_bluetooth_select)

		val blv = findViewById<ListView>(R.id.bluetoothNameList)

		val bAdapter = BluetoothAdapter.getDefaultAdapter()
		val bondedDevices = bAdapter.bondedDevices

		val itemList = ArrayList<BluetoothData>()
		for (device in bondedDevices) {
			val bluetoothData = BluetoothData(device.name, device.address)
			itemList.add(bluetoothData)
		}

		blv.adapter = BluetoothDataAdaptor(this, itemList)

		blv.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
			val intent = Intent()
			intent.putExtra(getString(R.string.bluetoothSelect), itemList[i].Name)
			setResult(RESULT_OK, intent)
			finish()
		}
	}
}

class BluetoothData(name: String, macAddress: String) {
	val Name = name
	val MacAddress = macAddress
}

class BluetoothDataAdaptor(context: Context, items: List<BluetoothData>) : BaseAdapter() {
	val Items = items
	val LayoutInflater = android.view.LayoutInflater.from(context)

	override fun getCount(): Int {
		return Items.size
	}

	override fun getItem(p0: Int): Any {
		return Items[p0]
	}

	override fun getItemId(p0: Int): Long {
		return p0.toLong();
	}

	override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
		val view = LayoutInflater.inflate(R.layout.bluetooth_listview_layout, null)

		val macView = view.findViewById<TextView>(R.id.mac_address)
		val nameView = view.findViewById<TextView>(R.id.bluetooth_name)

		val data = Items[p0]

		macView.text = data.MacAddress
		nameView.text = data.Name

		return view;
	}
}