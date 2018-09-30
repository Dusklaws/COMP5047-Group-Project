package comp5047.exmaster.unbend

import android.Manifest
import android.app.ListActivity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast


import comp5047.exmaster.unbend.R

class ScanActivity : ListActivity() {

    lateinit var mDeviceListAdapter: DeviceListAdapter
    lateinit var mBluetoothAdapter: BluetoothAdapter
    var mScanning : Boolean = false
    lateinit var mHandler: Handler
    private val SCAN_PERIOD: Long = 10000
    private lateinit var scanBtn : FloatingActionButton

    val mScanCallback = BluetoothAdapter.LeScanCallback{ bluetoothDevice: BluetoothDevice, i: Int, bytes: ByteArray ->
        runOnUiThread({
            mDeviceListAdapter.addDevice(bluetoothDevice)
            mDeviceListAdapter.notifyDataSetChanged()
        })
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        mHandler = Handler()
        scanBtn = findViewById(R.id.scanBtn)
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE Not supported, closing app", Toast.LENGTH_LONG).show()
            finish()
        }

        val bluetoothManager : BluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = bluetoothManager.adapter
        requestLocationPermission()

        if(mBluetoothAdapter == null){
            Toast.makeText(this, "BLE Not supported, closing app", Toast.LENGTH_LONG).show()
            finish()
            return
        }

    }


    override fun onResume() {
        super.onResume()
        if (!mBluetoothAdapter.isEnabled) {
            if (!mBluetoothAdapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, 100)
            }
        }

        // Initializes list view adapter.
        mDeviceListAdapter = DeviceListAdapter(this)
        listAdapter = mDeviceListAdapter
        scanDevice(true)
    }


    override fun onPause(){
        super.onPause()
        scanDevice(false)
        mDeviceListAdapter.clearDevice()
    }

    override fun finish() {
        super.finish()
        scanDevice(false)
        mDeviceListAdapter.clearDevice()
    }


    fun onScanClick(v : View){
        if(mScanning){
            scanDevice(false)
        }else{
            scanDevice(true)
        }
    }

    override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {
        super.onListItemClick(l, v, position, id)
        val device = mDeviceListAdapter.getItem(position)
        if(device == null) return
        val intent = Intent(this@ScanActivity, DeviceActivity::class.java)
        if(!device.name.contains("BBC") && !device.name.contains("micro:bit")){
            Toast.makeText(this,"Device is not a micro:bit", Toast.LENGTH_SHORT)
            return
        }
        intent.putExtra("deviceName", device.name)
        intent.putExtra("deviceAddress", device.address)
        if(mScanning){
            mScanning = false
            mBluetoothAdapter.stopLeScan(mScanCallback)
        }
        startActivity(intent)
    }

    fun scanDevice(enable : Boolean){
        if(enable){
            mScanning = true
            scanBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_cancel))
//            actionBar.title = "Scanning"
            mHandler.postDelayed({
                mScanning = false
//                  actionBar.title = "Not Scanning"
                    scanBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_refresh))
                mBluetoothAdapter.stopLeScan(mScanCallback)
            }, SCAN_PERIOD)
            scanBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_cancel))
            mScanning = true
            mBluetoothAdapter.startLeScan(mScanCallback)
        }else{
            mScanning = false
            scanBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_refresh))
//            actionBar.title = "Not Scanning"
            mBluetoothAdapter.stopLeScan(mScanCallback)
        }
    }


    fun requestLocationPermission(){
        if(ContextCompat.checkSelfPermission(this@ScanActivity,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 100)
            }
        }
    }



    class DeviceListAdapter(context : Context) : BaseAdapter(){
        lateinit var mDevices : ArrayList<BluetoothDevice>
        lateinit var mInflator : LayoutInflater


        init {
            mDevices = ArrayList()
            mInflator = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        }

        fun addDevice(bluetoothDevice: BluetoothDevice){
            if(!mDevices.contains(bluetoothDevice)) mDevices.add(bluetoothDevice)
        }

        fun clearDevice(){mDevices.clear()}

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            lateinit var viewHolder: ViewHolder
            lateinit var view : View
            view = mInflator.inflate(R.layout.list_device,null)
            view.findViewById<TextView>(R.id.device_name).text = mDevices[position].name
            view.findViewById<TextView>(R.id.device_address).text = mDevices[position].address


            return view
        }

        override fun getItem(position: Int) = mDevices.get(position)

        override fun getItemId(position: Int) = position.toLong()

        override fun getCount(): Int = mDevices.size
    }



    internal class ViewHolder {
        var deviceName: TextView? = null
        var deviceAddress: TextView? = null
    }

}