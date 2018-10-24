package comp5047.exmaster.unbend

import android.Manifest
import android.bluetooth.*
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import java.util.*
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGatt
import android.content.*
import android.content.pm.PackageManager
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.Menu
import android.view.MenuItem
import android.widget.ProgressBar


class DeviceActivity : AppCompatActivity(){



    var mConnected = false
    var mSetUp = false
    var mCalibrating = 10
    var mCalibrated = false

    lateinit var mDeviceAddress: String
    lateinit var mHandler : Handler

    lateinit var statusText : TextView
    lateinit var xText : TextView
    lateinit var yText : TextView
    lateinit var zText : TextView

    lateinit var xProgress : ProgressBar
    lateinit var yProgress : ProgressBar
    lateinit var zProgress : ProgressBar


    lateinit var connectBtn : Button
    lateinit var calibrateBtn : Button

    lateinit var mBluetoothDevice : BluetoothDevice
    lateinit var mBluetoothGatt: BluetoothGatt
    lateinit var mGattCallback: GattCallBack

    var bluetoothBroadCastReciever  = BluetoothBroadcastReceiver()



    var xCal = Pair(9999,0)
    var yCal = Pair(9999,0)
    var zCal = Pair(9999,0)


    class GattCallBack(context: Context): BluetoothGattCallback() {

        var UART_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E")
        var TX_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E")
        var RX_UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E")
        var CLIENT_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

        lateinit var tx : BluetoothGattCharacteristic
        lateinit var rx : BluetoothGattCharacteristic
        var mContext = context


        private fun broadcastData(action : String, data : String){
            val intent = Intent()
            intent.setAction(action)
            intent.putExtra("data", data)
            mContext.sendBroadcast(intent)
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    if (!gatt!!.discoverServices()) {
                        Log.d("Test", "Failed to discover services")
                        broadcastData("comp5047.exmaster.unbend.ISSUES","Status: Issues with services")
                    }
                } else {
                    Log.d("Test", "Failed to connect")
                    broadcastData("comp5047.exmaster.unbend.ISSUES","Status: Issues with device")

                }
            }else{
                Log.d("Test", "Disconnected")
                broadcastData("comp5047.exmaster.unbend.DISCONNECTED","")
            }

        }





        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            gatt!!.services.forEach{service ->
                Log.d("Test", service.uuid.toString())
            }

            tx = gatt.getService(UART_UUID).getCharacteristic(TX_UUID)
            rx = gatt.getService(UART_UUID).getCharacteristic(RX_UUID)

            if(rx == null || tx == null){
                Log.d("Test" , "Failed to get characteristics")
                broadcastData("comp5047.exmaster.unbend.ISSUES","Status: Issues with characteristics")
                return
            }

            if(!gatt.setCharacteristicNotification(tx, true)){
                Log.d("Test", "Set Characteristic failed")
                broadcastData("comp5047.exmaster.unbend.ISSUES","Status: Issues with characteristics")
                return
            }
//            tx.descriptors.forEach{d ->
//                Log.d("Test Descriptor", d.uuid.toString())
//            }

            if (tx.getDescriptor(CLIENT_UUID) != null) {
                val desc = tx.getDescriptor(CLIENT_UUID)
                desc.value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                if (!gatt.writeDescriptor(desc)) {
                    Log.d("Test", "Couldn't write RX client descriptor value!")
                    broadcastData("comp5047.exmaster.unbend.ISSUES","Status: Issues with descriptor")
                    return
                }else{
                    broadcastData("comp5047.exmaster.unbend.CONNECTED","")
                }
            } else {
                Log.d("Test","Couldn't get RX client descriptor!")
                broadcastData("comp5047.exmaster.unbend.ISSUES","Status: Issues with descriptor")
                return
            }
        }



        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicChanged(gatt, characteristic)
            val s = characteristic!!.getStringValue(0)
//            Log.d("Test" ,s)
            broadcastData("comp5047.exmaster.unbend.DATA",s)

        }

    }


    fun onCalibrate(v : View){
        calibrateBtn.isEnabled = false
        connectBtn.isEnabled = false
        mCalibrated = false
        Toast.makeText(this, "Calibrating, adjust your head to the healthy position", Toast.LENGTH_SHORT).show()
        mCalibrating = 10
        calibrate()
    }

    fun calibrate(){
        if(mCalibrating > 0){
            statusText.text = "Calibrating please wait " + mCalibrating.toString() + " seconds"
            mCalibrating -= 1
            mHandler.postDelayed({
                calibrate()
            }, 1000)
        }else{
            Toast.makeText(this, "Calibration complete", Toast.LENGTH_SHORT).show()
            connectBtn.isEnabled = true
            calibrateBtn.isEnabled = true
            statusText.text = "Calibrated"
            Log.d("Test", xCal.toString())
            Log.d("Test", yCal.toString())
            Log.d("Test", zCal.toString())
            mCalibrated = true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 200 && resultCode ==100){
            mDeviceAddress = data!!.getStringExtra("deviceAddress")
            findViewById<TextView>(R.id.deviceName).text = mDeviceAddress
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mHandler = Handler()
        setContentView(R.layout.activity_device)
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE Not supported, closing", Toast.LENGTH_LONG).show()
            finish()
        }
        requestLocationPermission()

        mDeviceAddress = "CE:8D:8C:73:9E:40"
        findViewById<TextView>(R.id.deviceName).text = mDeviceAddress
        statusText = findViewById(R.id.status)
        xText = findViewById(R.id.x)
        yText = findViewById(R.id.y)
        zText = findViewById(R.id.z)
        xProgress = findViewById(R.id.xProgress)
        yProgress = findViewById(R.id.yProgress)
        zProgress = findViewById(R.id.zProgress)

        statusText.text = "Device not setup"
        calibrateBtn = findViewById(R.id.calibrateBtn)
        connectBtn = findViewById(R.id.connectBtn)
        connectBtn.isEnabled = false
        calibrateBtn.isEnabled = false

        val intentFilter = IntentFilter()
        intentFilter.addAction("comp5047.exmaster.unbend.DATA")
        intentFilter.addAction("comp5047.exmaster.unbend.CONNECTED")
        intentFilter.addAction("comp5047.exmaster.unbend.DISCONNECTED")
        intentFilter.addAction("comp5047.exmaster.unbend.ISSUES")
        this.registerReceiver(bluetoothBroadCastReciever,intentFilter )

    }

    fun onSetUp(v:View){
        val bluetoothManager : BluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        mBluetoothDevice = bluetoothAdapter.getRemoteDevice(mDeviceAddress)
        mGattCallback = GattCallBack(this@DeviceActivity)
        mSetUp = true
        findViewById<Button>(R.id.setUpButton).isEnabled = false
        statusText.text = "Ready to connect"
        connectBtn.isEnabled =true
    }


    fun onConnectClick(v : View){
        if(mConnected){
            mBluetoothGatt.disconnect()
        }else{
            mBluetoothGatt = mBluetoothDevice.connectGatt(this, true, mGattCallback)
        }
    }



    fun parseData(s : String){
        Log.d("Test",s)

        var accel = s.trim()
        var values = accel.trim().split("|")
        var x = values.get(0).toInt()
        var y = values.get(1).toInt()
        var z = values.get(2).toInt()

        xText.text = x.toString()
        yText.text = y.toString()
        zText.text = z.toString()
        xProgress.progress = x + 2048
        yProgress.progress = y + 2048
        zProgress.progress = z + 2048

        if(mCalibrating > 0){
            if(x <= xCal.first) xCal = Pair(x, xCal.second)
            if(x >= xCal.second) xCal = Pair(xCal.first, x)

            if(y <= yCal.first) yCal = Pair(y, yCal.second)
            if(y >= yCal.second) yCal = Pair(yCal.first, y)

            if(z <= zCal.first) zCal = Pair(z, zCal.second)
            if(z >= zCal.second) zCal = Pair(zCal.first, z)
        }

        if(mCalibrated){
            if(x < xCal.first){
                Toast.makeText(this, "Adjust your head right", Toast.LENGTH_SHORT).show()
            }else if( x > xCal.second){
                Toast.makeText(this, "Adjust your head left", Toast.LENGTH_LONG).show()

            }
            if(y < yCal.first){
                Toast.makeText(this, "Adjust your head forward", Toast.LENGTH_LONG).show()
            }else if( y > yCal.second){
                Toast.makeText(this, "Adjust your head backward", Toast.LENGTH_LONG).show()

            }

        }
    }

    fun onConnected(){
        statusText.text = "Status: Connected"
        connectBtn.text = "Disconnect"
        calibrateBtn.isEnabled = true
        mConnected = true
    }

    fun onDisconnected(){
        statusText.text = "Status: Disconnected"
        connectBtn.text = "Connect"
        calibrateBtn.isEnabled = false
        mConnected = false
    }

    fun onIssues(s:String){
        Toast.makeText(this, "Failed to connect", Toast.LENGTH_SHORT).show()
        statusText.text = s
        connectBtn.text = "Connect"
        mConnected = false
    }

    override fun  onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item!!.itemId){
            (R.id.action_find) ->{
                val intent = Intent(this, ScanActivity::class.java)
                startActivityForResult(intent, 200)
                mConnected = false
                mSetUp = false
                connectBtn.isEnabled = false
                calibrateBtn.isEnabled = false
                findViewById<Button>(R.id.setUpButton).isEnabled = true
                if(mConnected) mBluetoothGatt.disconnect()
                statusText.text = "Please press set up again"
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun requestLocationPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 100)
            }
        }
    }

}