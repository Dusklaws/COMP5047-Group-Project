package comp5047.exmaster.unbend

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.widget.Toast


class BluetoothBroadcastReceiver : BroadcastReceiver() {



    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val mContext = context as DeviceActivity
        Toast.makeText(context, "Test",Toast.LENGTH_SHORT)
        when(action){
            "comp5047.exmaster.unbend.DATA" -> mContext.parseData(intent.getStringExtra("data"))
            "comp5047.exmaster.unbend.CONNECTED" -> mContext.onConnected()
            "comp5047.exmaster.unbend.DISCONNECTED" -> mContext.onDisconnected()
            "comp5047.exmaster.unbend.ISSUES" -> mContext.onIssues(intent.getStringExtra("data"))
        }
    }
}