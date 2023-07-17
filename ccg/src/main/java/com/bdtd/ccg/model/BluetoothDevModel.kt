package com.bdtd.ccg.model

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanRecord

@SuppressLint("MissingPermission")
data class BluetoothDevModel (
    val dev: BluetoothDevice,
    var connected: Boolean = false, // 是否已连接
) {

}
