package com.bdtd.ccg

import android.content.Context
import android.util.Log

object CcgHelper {

    /**
     * @Description: 打开蓝牙弹窗
     * @param:       autoRead : 自动读取
     *               当autoRead = true时，表示连接成功后，自动读取手持便携仪数据
     *               当autoRead = false时，表示连接成功后，需要点击"读取数据"按钮，然后才会读取手持便携仪数据
     * @Author:      zhanghh
     * @CreateDate:  2023/7/10 15:39
     */
    fun openBluetoothDialog(context: Context, autoRead: Boolean) {
        val bluetoothUtils = BluetoothClientUtils.instance
        bluetoothUtils.initBlueTooth(context)
        if (bluetoothUtils.isSupport()) {
            BluetoothDialog.instance.show(context, bluetoothUtils, autoRead)
            BluetoothDialog.instance.getConnectedDev()
            BluetoothDialog.instance.setJD5DialogListener(object : BluetoothDialog.JD5DialogListener {
                override fun fillData(data: JD5ReceiveModel) {
                    Log.i("main", "data.time-->${data.time}")
                    Log.i("main", "data.methaneVal-->${data.methaneVal}")
                    Log.i("main", "data.co-->${data.co}")
                    Log.i("main", "data.o2-->${data.o2}")
                    Log.i("main", "data.temp-->${data.temp}")
                    Log.i("main", "data.co2-->${data.co2}")
//                        viewBinding.inspectDetailsDateContent.text = data.time // JD5传过来的月份是0会报错，暂时先使用手机时间
//                    viewBinding.inspectMethaneContent.text = data.methaneVal
//                    viewBinding.inspectMethaneEdit.setText(data.methaneVal)
//                    viewBinding.inspectCoContent.text = data.co
//                    viewBinding.inspectCoEdit.setText(data.co)
//                    viewBinding.inspectO2Content.text = data.o2
//                    viewBinding.inspectO2Edit.setText(data.o2)
//                    viewBinding.inspectTemperatureContent.text = data.temp
//                    viewBinding.inspectTemperatureEdit.setText(data.temp)
//                    viewBinding.inspectCo2Content.text = data.co2
//                    viewBinding.inspectCo2Edit.setText(data.co2)
                }
            })
        }
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
}