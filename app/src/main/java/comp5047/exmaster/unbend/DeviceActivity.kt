package comp5047.exmaster.unbend

import android.content.ServiceConnection
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity

class DeviceActivity : AppCompatActivity() {

    val UARTSERVICE_SERVICE_UUID = "6E400001B5A3F393E0A9E50E24DCCA9E"
    val UART_RX_CHARACTERISTIC_UUID = "6E400002B5A3F393E0A9E50E24DCCA9E"
    val UART_TX_CHARACTERISTIC_UUID = "6E400003B5A3F393E0A9E50E24DCCA9E"




    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.activity_device)


    }

    override fun onResume() {
        super.onResume()
    }


    



}