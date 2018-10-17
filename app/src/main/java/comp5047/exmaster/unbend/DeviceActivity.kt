package comp5047.exmaster.unbend

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
import android.widget.ProgressBar


class DeviceActivity : AppCompatActivity(){



    var mConnected = false
    lateinit var mDeviceAddress: String
    lateinit var mDeviceName : String

    lateinit var statusText : TextView
    lateinit var xText : TextView
    lateinit var yText : TextView
    lateinit var zText : TextView

    lateinit var xProgress : ProgressBar
    lateinit var yProgress : ProgressBar
    lateinit var zProgress : ProgressBar



    lateinit var connectBtn : Button

    lateinit var mBluetoothDevice : BluetoothDevice
    lateinit var mBluetoothGatt: BluetoothGatt
    lateinit var mGattCallback: GattCallBack

    var bluetoothBroadCastReciever  = BluetoothBroadcastReceiver()


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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device)
        mDeviceAddress = intent.getStringExtra("deviceAddress")
        mDeviceName = intent.getStringExtra("deviceName")


        findViewById<TextView>(R.id.deviceName).text = mDeviceAddress
        statusText = findViewById(R.id.status2)
        xText = findViewById(R.id.x)
        yText = findViewById(R.id.y)
        zText = findViewById(R.id.z)
        xProgress = findViewById(R.id.xProgress)
        yProgress = findViewById(R.id.yProgress)
        zProgress = findViewById(R.id.zProgress)


        connectBtn = findViewById(R.id.connectBtn)

        val bluetoothManager : BluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        mBluetoothDevice = bluetoothAdapter.getRemoteDevice(mDeviceAddress)
        mGattCallback = GattCallBack(this@DeviceActivity)
        val intentFilter = IntentFilter()
        intentFilter.addAction("comp5047.exmaster.unbend.DATA")
        intentFilter.addAction("comp5047.exmaster.unbend.CONNECTED")
        intentFilter.addAction("comp5047.exmaster.unbend.DISCONNECTED")
        intentFilter.addAction("comp5047.exmaster.unbend.ISSUES")
        this.registerReceiver(bluetoothBroadCastReciever,intentFilter )

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
        var x : Int = xText.text.toString().toInt()
        var y : Int = yText.text.toString().toInt()
        var z : Int = zText.text.toString().toInt()
        val trimmed = s.trim()
        when(trimmed.get(0)){
            'x'-> x =  trimmed.substring(2).toInt()
            'y'-> y = trimmed.substring(2).toInt()
            'z'-> z = trimmed.substring(2).toInt()
        }
        xText.text = x.toString()
        yText.text = y.toString()
        zText.text = z.toString()
        xProgress.progress = x + 2048
        yProgress.progress = y + 2048
        zProgress.progress = z + 2048


    }

    fun onConnected(){
        statusText.text = "Status: Connected"
        connectBtn.text = "Disconnect"
        mConnected = true
    }

    fun onDisconnected(){
        statusText.text = "Status: Disconnected"
        connectBtn.text = "Connect"
        mConnected = false
    }

    fun onIssues(s:String){
        Toast.makeText(this, "Failed to connect", Toast.LENGTH_SHORT).show()
        statusText.text = s
        connectBtn.text = "Connect"
        mConnected = false
    }




}