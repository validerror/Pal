package com.linmu.pal.broadcasts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager

class BatteryChargeReceiver(
    private val charging:() -> Unit,
    private val unCharge: () -> Unit
) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val batteryStatus = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        if (batteryStatus != BatteryManager.BATTERY_STATUS_CHARGING) {
            unCharge()
        } else {
            charging()
        }
    }
}