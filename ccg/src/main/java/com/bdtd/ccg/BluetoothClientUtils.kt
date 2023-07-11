package com.bdtd.ccg

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import com.bdtd.ccg.common.HexUtil
import java.util.*

/**
 *
 * @Description:    BLE蓝牙客户端(中心设备)
 * @Author:         zhanghh
 * @CreateDate:     2022/10/26 09:21
 */

@SuppressLint("MissingPermission")
class BluetoothClientUtils private constructor() {
    private val TAG = "BluetoothClientUtils"

    private var context: Context? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var blueToothManager: BluetoothManager? = null
    private var bluetoothGatt: BluetoothGatt? = null

    private val mHandler = Handler(Looper.getMainLooper())
    private var isConnected = false
    private var isScanning = false // 是否正在扫描
    private var isReturnResult = false // APP向JD5发送数据后，JD5是否已返回给APP数据
    private var delayMillis = 8000L // 8秒

    private var mConnectListener: BluetoothConnectListener? = null // 关于蓝牙连接的回调
    private var mCommListener: BluetoothCommListener? = null // 关于手机和JD5通信的回调

    /**
     * UUID总长度可以为32或16。下面的UUID包含了4个"-"，因此实际长度为32。相当于32个16进制数的组合
     * 32个十六进制数 = 128个位 = 16个字节
     */
    val JD5_UUID_SERVICE = UUID.fromString("00008e20-0000-1000-8000-00805f9b34fb") // 外围设备蓝牙ID
    val JD5_UUID_READ_NOTIFY = UUID.fromString("00008e32-0000-1000-8000-00805f9b34fb") // 外围设备蓝牙ID
    val JD5_UUID_WRITE = UUID.fromString("00008e40-0000-1000-8000-00805f9b34fb") // "写"的指令

    companion object {
        val instance: BluetoothClientUtils by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            BluetoothClientUtils()
        }
    }

    /**
     * 初始化蓝牙
     */
    fun initBlueTooth(context: Context) {
        this.context = context
        if(blueToothManager == null) blueToothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?
        if(bluetoothAdapter == null) bluetoothAdapter = blueToothManager?.adapter
    }

    /**
     * 是否支持蓝牙
     */
    fun isSupport(): Boolean {
        if (isSupportBLE()) {
            // 支持BLE蓝牙
            if (isOpenBluetooth()) { // 蓝牙是否已开启
                return isOpenGPS() // GPS是否开启
            } else {
                return false // 蓝牙未开启
            }
        } else {
            Toast.makeText(context, "当前设备不支持BLE蓝牙", Toast.LENGTH_SHORT).show()
            return false
        }
    }

    /**
     * 扫描蓝牙
     */
    fun scanDev() {
        if (blueToothManager == null || bluetoothAdapter == null) {
            Toast.makeText(context, "蓝牙初始化失败", Toast.LENGTH_SHORT).show()
            return
        }
        if (isScanning) {
            return
        }
        isScanning = true
        // 扫描很耗电，因此需要设置扫描时间
        mHandler.postDelayed({
            if (isScanning) {
                bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanListener)
                mConnectListener?.scanStop()
                isScanning = false
            }
        }, delayMillis) // 8秒
        bluetoothAdapter?.bluetoothLeScanner?.startScan(scanListener)
        // 如果想要扫描特定类型的设备，可以通过执行startScan(List<ScanFilter> filters, ScanSettings settings, ScanCallback callback)方法
    }

    /**
     * 扫描设备的回调
     */
    private val scanListener = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            //不断回调，所以不建议做复杂的动作
            if (result == null || result.device.name == null) {
                return
            }

            if (isScanning) {
                Log.i("main", "扫描到的设备-->${result.device.name}")
                if (BluetoothHelpUtils.getInstance().isJD5Device(result.device)) {
                    val bean = BluetoothDevModel(result.device)
                    mConnectListener?.scanResult(bean)
                }
            }
        }
    }

    /**
     * 连接蓝牙
     *
     * 未连接的设备执行此方法时，会连接蓝牙
     *
     * 已连接的设备执行此方法时，相当于判断蓝牙是否连接，并不会重新连接蓝牙
     * 已连接状态下，执行connectGatt()并非为了连接蓝牙，而是为了获取“蓝牙连接”的对象
     * 已连接状态下，connectGatt()不会重新连接蓝牙，会直接回调onConnectionStateChange()
     * 在服务已连接的状态下，gatt?.discoverServices()与此同理，并不会执行“发现服务”操作，而是直接执行onServicesDiscovered()
     */
    fun connect(dev: BluetoothDevModel, isJd5: Boolean) {
        isScanning = false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            bluetoothGatt = dev.dev.connectGatt(context, false, blueGattListener, BluetoothDevice.TRANSPORT_LE)
        } else {
            bluetoothGatt = dev.dev.connectGatt(context, false, blueGattListener)
        }
    }

    /**
     * JD5写入数据
     */
    fun writeToJD5(data: String) {
        val service = getGattService(JD5_UUID_SERVICE)
        if (service != null) {
            val characteristic = service.getCharacteristic(JD5_UUID_WRITE) //通过UUID获取可读的Characteristic
            if (characteristic != null) {
                bluetoothGatt?.setCharacteristicNotification(characteristic, true)
                characteristic.descriptors[0].value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                isReturnResult = false
                openJD5NotifyServer()
                characteristic.value = HexUtil.hexToByte(data)
                bluetoothGatt?.writeCharacteristic(characteristic)
            } else {
                logInfo("写入失败")
            }
        }
    }

    // 获取Gatt服务
    private fun getGattService(uuid: UUID?): BluetoothGattService? {
        if (!isConnected) {
            logInfo("没有连接")
            return null
        }
        val service = bluetoothGatt?.getService(uuid)
        if (service == null) {
            logInfo("没有找到服务")
        }
        return service
    }

    private val blueGattListener = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            val device = gatt?.device
            if (newState == BluetoothProfile.STATE_CONNECTED){
                isConnected = true
                //开始发现服务，有个小延时，最后200ms后尝试发现服务
                mHandler.postDelayed({
                    gatt?.discoverServices()
                },300)

                device?.let{logInfo("与 ${it.name} 连接成功!!!")}
            }else if (newState == BluetoothProfile.STATE_DISCONNECTED){
                isConnected = false
                mConnectListener?.disConnect()
                logInfo("无法与 ${device?.name} 连接: $status")
                closeConnect()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                bluetoothGatt = gatt
                mConnectListener?.connected()
                gatt?.requestMtu(512) // 设置最大发送数据长度为512个字节
                logInfo("已连接上 GATT 服务，可以通信! ")
            } else {
                closeConnect()
                logInfo("连接服务失败，请点击设备重新连接：${status}")
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            characteristic?.let {
                val data = String(it.value)

                var sb = StringBuilder()
                it.value.forEach { byte ->
                    sb.append(byte)
                }

                (context as Activity).runOnUiThread {
                    mCommListener?.readResult(data)
                }
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            characteristic?.let {
                /**
                 * 因为APP向JD5发送数据后，有时JD5返回校验结果的onCharacteristicChanged回调会在onCharacteristicWrite之前回调，因此添加isReturnResult判断
                 */
                if (!isReturnResult) {
                    mCommListener?.writeSuccess()
                }
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            characteristic?.let {
                isReturnResult = true
                if (it.value != null && it.value.isNotEmpty()) {
                    var data = HexUtil.byteToHexStr(it.value)
                    try {
                        val header = data.substring(0, 4)
//                        val data = String(it.value)
//                        val header = String.format("%02X", it.value[0]) + String.format("%02X", it.value[1])
                        when (header) {
                            "0104" -> {
                                mCommListener?.jd5AutoGasData(data) // JD5主动上传数据
                            }
                            "0142" -> { // JD5手动上传数据
                                mCommListener?.jd5HandGasData(data)
                            }
                            "0110" -> { // MODBUS协议的应答
                                mCommListener?.goBackResult(data)
                            }
                            else -> {
                                mCommListener?.goBackResult(data)
                            }
                        }
                    } catch (e: Exception) {
                        mCommListener?.goBackResult(data)
                    }
                } else {
                    mCommListener?.goBackResult("")
                }
            }
        }

        override fun onDescriptorRead(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            super.onDescriptorRead(gatt, descriptor, status)
            descriptor?.let {
                val data = String(it.value)
                logInfo("DescriptorRead 数据: $data")
            }
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            super.onDescriptorWrite(gatt, descriptor, status)
            descriptor?.let {
                val data = String(it.value)
                logInfo("DescriptorWrite 数据: $data")
            }
        }
    }

    /**
     * 打开JD5通知监听，收到通知后会在onCharacteristicChanged中回调
     */
    private fun openJD5NotifyServer() {
        val service = getGattService(JD5_UUID_SERVICE)
        if (service != null) {
            val characteristic =
                service.getCharacteristic(JD5_UUID_READ_NOTIFY) //通过UUID获取可读的Characteristic
            if (characteristic != null) {
                bluetoothGatt?.setCharacteristicNotification(characteristic, true)

                val descriptors = characteristic.descriptors
                descriptors.forEach {
                    it.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                }
            }
        }
    }

    /**
     * 获取已连接的设备
     *
     * devClass
     * 1.JD5设备
     * 2.白板设备
     */
    fun getConnectedDevice(): List<BluetoothDevModel>? {
        return BluetoothHelpUtils.getInstance().getConnectedJD5Devices(blueToothManager)
    }

    private fun logInfo(msg: String) {
        (context as Activity).runOnUiThread {
            mConnectListener?.logInfo(msg)
        }
    }

    /**
     * 关闭连接
     */
    fun closeConnect() {
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanListener)
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
    }

    /**
     * 检测手机是否支持BLE蓝牙
     */
    private fun isSupportBLE(): Boolean {
        return context?.packageManager?.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)!!
    }

    /**
     * 判断蓝牙是否开启
     *
     * 如果蓝牙未开启，则在底部弹出开启蓝牙弹框
     */
    private fun isOpenBluetooth(): Boolean {
        if (bluetoothAdapter == null || bluetoothAdapter?.isEnabled == false) {
            // 开启蓝牙(底部弹窗)
            (context as Activity).startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 1)
            return false
        } else {
            return true
        }
    }

    /**
     * 判断是否打开GPS
     *
     * Android10及10以上系统，需要GPS权限
     */
    private fun isOpenGPS(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val lm = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                TipDialog.instance.show(context!!, "需要开启gps，否则蓝牙不可用！点击确定去开启。")
                TipDialog.instance.setOnTipDialogListener(object : TipDialog.OnTipDialogListener {
                    override fun onCancelClick() {
                    }

                    override fun onConfirmClick() {
                        val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context?.startActivity(settingsIntent)
                    }
                })
                return false
            } else {
                return true
            }
        } else {
            return true
        }
    }

    fun setDelayMillis(millis: Long) {
        this.delayMillis = millis
    }

    /**
     * 释放资源
     */
    fun release() {
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanListener)
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        mConnectListener = null
        mCommListener = null
    }

    /**
     * 取消所有已配对的设备
     */
    fun removePairDevice() {
        BluetoothHelpUtils.getInstance().removePairDevice(context, bluetoothAdapter)
    }

    /**
     * 只关闭耦合，不关闭蓝牙连接
     */
    fun limitRelease() {
        removePairDevice()
        mConnectListener = null
        mCommListener = null
        this.context = null
    }

    fun setConnectListener(listener: BluetoothConnectListener) {
        mConnectListener = listener
    }

    fun setCommListener(listener: BluetoothCommListener) {
        mCommListener = listener
    }

    interface BluetoothConnectListener {
        fun scanResult(dev: BluetoothDevModel) // 扫描到的设备

        fun scanStop() // 扫描结束

        fun connected() // 连接成功

        fun disConnect() // 断开连接

        fun logInfo(msg: String) // 操作信息
    }

    interface BluetoothCommListener {
        fun readResult(data: String) // 读取到的数据

        fun writeSuccess() // 写入成功

        fun goBackResult(data: String) // JD5或白板返回的数据

        fun jd5AutoGasData(data: String) // JD5自动发送给APP的气体数据

        fun jd5HandGasData(data: String) // JD5手动发送给APP的气体数据
    }
}