package kr.ad960009.bluetoothcheck

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class ApplicationListActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_application_list)

		val pm = packageManager
		val list = pm.getInstalledApplications(PackageManager.GET_META_DATA)


		val itemList = ArrayList<ApplicationData>()

		for (item in list) {
			val appData = ApplicationData(
				item.packageName,
				item.loadLabel(pm).toString(),
				item.loadIcon(pm)
			)
			Log.d("ad960009", appData.PackageName + "/" + appData.Label)
			val intent = pm.getLaunchIntentForPackage(appData.PackageName)
			if (intent != null) {
				Log.d("ad960009", intent.action)
				itemList.add(appData)
			}
		}

		val listView = findViewById<ListView>(R.id.application_list_view)
		listView.adapter = ApplicationDataAdaptor(this, itemList)
		listView.onItemClickListener =
			AdapterView.OnItemClickListener { adapterView: AdapterView<*>?, view: View?, i: Int, l: Long ->
				Log.d("ad960009", itemList[i].PackageName + " Selected")
				val intent = Intent()
				intent.putExtra(getString(R.string.packageSelect), itemList[i].PackageName)
				setResult(RESULT_OK, intent)
				finish()
			}
	}
}


class ApplicationData(packageName: String, label: String, icon: Drawable) {
	val PackageName = packageName
	val Label = label
	val Icon = icon
}

class ApplicationDataAdaptor(context: Context, items: List<ApplicationData>) : BaseAdapter() {
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
		val view = LayoutInflater.inflate(R.layout.app_listview_layout, null)

		val iconView = view.findViewById<ImageView>(R.id.image_view)
		val labelView = view.findViewById<TextView>(R.id.application_name)
		val packageView = view.findViewById<TextView>(R.id.package_name)

		val data = Items[p0]

		iconView.setImageDrawable(data.Icon)
		labelView.text = data.Label
		packageView.text = data.PackageName

		return view;
	}
}