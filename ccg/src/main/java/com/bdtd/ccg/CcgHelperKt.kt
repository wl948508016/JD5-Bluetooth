package com.bdtd.ccg

import android.content.Context
import com.bdtd.ccg.model.JD5ReceiveModel
import com.bdtd.ccg.ui.BluetoothDialog
import com.bdtd.ccg.utils.BluetoothClientUtils
import com.bdtd.ccg.utils.PermissionUtils

/**
 * 由于回调需要使用到RN的组件，因此将回调的代码放在RN项目中，不放在此项目中
 */

class CcgHelperKt private constructor() {
    
    companion object {
        val instance: CcgHelperKt by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            CcgHelperKt()
        }
    }

    /**
     * @Description: 打开蓝牙弹窗
     * @param:       autoRead : 自动读取
     *               当autoRead = true时，表示连接成功后，自动读取手持便携仪数据
     *               当autoRead = false时，表示连接成功后，需要点击"读取数据"按钮，然后才会读取手持便携仪数据
     * @Author:      zhanghh
     * @CreateDate:  2023/7/10 15:39
     */
    fun openBluetoothDialog(context: Context, autoRead: Boolean, listener: OnCcgHelperKtListener?) {
//    fun openBluetoothDialog(context: Context, autoRead: Boolean) {
        PermissionUtils.instance.requestPermission(context, object : PermissionUtils.OnPermissionUtilsListener {
            override fun onPermissionSuccess() {
                val bluetoothUtils = BluetoothClientUtils.instance
                bluetoothUtils.initBlueTooth(context)
                if (bluetoothUtils.isSupport()) {
                    BluetoothDialog.instance.show(context, bluetoothUtils, autoRead)
                    BluetoothDialog.instance.getConnectedDev()
                    BluetoothDialog.instance.setJD5DialogListener(object : BluetoothDialog.JD5DialogListener {
                        override fun fillData(data: JD5ReceiveModel) {
                            listener?.onResultData(data.methaneVal, data.co, data.o2, data.temp, data.co2)
                        }
                    })
                }
            }
        })
    }

    /**
     * @Description: 释放部分资源(建议频繁执行)
     *               关闭接口等耦合，不关闭蓝牙连接，不断开已连接的蓝牙，建议每次获取瓦斯巡检数据后执行
     * @Author:      zhanghh
     * @CreateDate:  2023/7/10 15:39
     */
    fun limitRelease() {
        BluetoothDialog.instance.release()
        BluetoothClientUtils.instance.limitRelease()
    }

    /**
     * @Description: 释放全部资源(可以不执行)
     *               断开所有已连接的蓝牙，释放蓝牙对象，建议退出app时执行
     * @Author:      zhanghh
     * @CreateDate:  2023/7/10 15:39
     */
    fun release() {
        BluetoothClientUtils.instance.release()
    }

    /**
     * @Description: 设置每次扫描时长(默认8秒，可以不设置)
     * @params:      delayMillis : 单位毫秒 (即8秒 = 8000)
     * @Author:      zhanghh
     * @CreateDate:  2023/7/10 15:39
     */
    fun setDelayMillis(delayMillis: Long) {
        BluetoothClientUtils.instance.setDelayMillis(delayMillis)
    }

    interface OnCcgHelperKtListener {
        fun onResultData(methaneVal: String, co: String, o2: String, temp: String, co2: String)
    }
    
}