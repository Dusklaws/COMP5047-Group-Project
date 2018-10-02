package comp5047.exmaster.unbend

import android.app.Activity
import android.bluetooth.*
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.location.Address
import android.os.Bundle
import android.os.IBinder
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import org.w3c.dom.Text
import java.util.*
import android.bluetooth.BluetoothGattService
import comp5047.exmaster.unbend.R.id.*
import android.bluetooth.BluetoothGattDescriptor
import android.system.Os.poll
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGatt
import comp5047.exmaster.unbend.R.id.status
import kotlin.concurrent.thread


abstract class AppCompatActivity

class DeviceActivity : AppCompatActivity(){



    var mConnected = false
    lateinit var mDeviceAddress: String
    lateinit var mDeviceName : String

    lateinit var statusText : TextView
    lateinit var xText : TextView
    lateinit var yText : TextView
    lateinit var zText : TextView
    lateinit var connectBtn : Button

    lateinit var mBluetoothDevice : BluetoothDevice
    lateinit var mBluetoothGatt: BluetoothGatt
    lateinit var mGattCallback: GattCallBack


    class GattCallBack : BluetoothGattCallback() {

        var UART_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E")
        var TX_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E")
        var RX_UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E")
        var CLIENT_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

        lateinit var tx : BluetoothGattCharacteristic
        lateinit var rx : BluetoothGattCharacteristic
        var mConnected = false

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    if (!gatt!!.discoverServices()) {
                        Log.d("Test", "Failed to discover services")
                    }
                } else {
                    Log.d("Test", "Failed to connect")
                }
            }else{
                mConnected = false
                Log.d("Test", "Disconnected")
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
            }

            if(!gatt.setCharacteristicNotification(tx, true)){
                Log.d("Test", "Set Characteristic failed")
                mConnected = false
            }
            tx.descriptors.forEach{d ->
                Log.d("Test Descriptor", d.uuid.toString())
            }

            if (tx.getDescriptor(CLIENT_UUID) != null) {
                val desc = tx.getDescriptor(CLIENT_UUID)
                desc.value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                if (!gatt.writeDescriptor(desc)) {
                    mConnected = false
                    Log.d("Test", "Couldn't write RX client descriptor value!")
                }
            } else {
                mConnected = false
                Log.d("Test","Couldn't get RX client descriptor!")
            }
        }





        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicChanged(gatt, characteristic)
            Log.d("Test chanted" ,characteristic!!.getStringValue(0))

        }


    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device)
        mDeviceAddress = intent.getStringExtra("deviceAddress")
        mDeviceName = intent.getStringExtra("deviceName")


        findViewById<TextView>(R.id.deviceName).text = mDeviceAddress
        statusText = findViewById(R.id.status)
        xText = findViewById(R.id.x)
        yText = findViewById(R.id.y)
        zText = findViewById(R.id.z)
        connectBtn = findViewById(R.id.connectBtn)

        val bluetoothManager : BluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        mBluetoothDevice = bluetoothAdapter.getRemoteDevice(mDeviceAddress)
        mGattCallback = GattCallBack()
    }

    override fun onResume() {
        super.onResume()
    }

    fun onConnectClick(v : View){
        if(mConnected){
            mConnected = false
            disconnect()
        }else{
            if(!connect()){
                Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show()
                return
            }
            runOnUiThread{
                statusText.text = "Status Connected"
                connectBtn.text = "Disconnect"
            }
            mConnected = true
        }
    }

    private fun connect() : Boolean{
        mBluetoothGatt = mBluetoothDevice.connectGatt(this, true, mGattCallback)
        if(mGattCallback.mConnected){
            return true
        }
        return false

    }

    fun disconnect(){

    }







}