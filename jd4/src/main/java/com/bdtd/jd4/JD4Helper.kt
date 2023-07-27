package com.bdtd.jd4

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.bdtd.jd4.model.JD4ReceiveModel
import com.bdtd.jd4.portable.activity.PortableActivity
import com.bdtd.jd4.utils.MsgDialog
import com.bdtd.jd4.utils.NetUtils

class JD4Helper private constructor() {
    private var mPort = "8087"

    companion object {
        val instance: JD4Helper by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            JD4Helper()
        }
    }

    /**
     * 启动app(服务端)，等待客户端发送数据
     */
    fun openUDPServer(context: Context, listener: OnUDPHelperListener) {
        UDPServer.instance.receive(context, mPort, object : UDPServer.OnUDPServerListener {
            override fun logInfo(msg: String) {
                // msg: 打开服务端时，返回的信息
                listener.logInfo(msg)
            }

            override fun onResult(clientIp: String, clientPort: String, jd4Data: JD4ReceiveModel) {
                // 获取到的瓦斯数据
                listener.result(clientIp, clientPort, jd4Data.methaneVal, jd4Data.co, jd4Data.o2, jd4Data.temp)
            }
        })
    }

    /**
     * 打开显示IP和端口的弹窗
     *
     * 仅作为IP和端口的弹窗提示。不需要打开时，可以不打开
     */
    fun openDialog(context: Context) {
        val ipAddress = getIPAddress(context)
        MsgDialog.instance.show(context, ipAddress, mPort)
    }

    /**
     * 关闭UDP服务端
     *
     * 不关闭UDP服务端时，app会持续收到瓦斯数据
     */
    fun closeUDPServer() {
        UDPServer.instance.closeUDP()
    }

    /**
     * 设置端口(默认8087)，建议不设置
     */
    fun setPort(port: String) {
        mPort = port
    }

    /**
     * 获取手机IP地址
     */
    fun getIPAddress(context: Context): String {
        return NetUtils.getInstance().getIpAddress(context)
    }

    /**
     * 获取手机UDP端口
     */
    fun getPort(): String {
        return mPort
    }

    /**
     * 打开配置JD4页面
     */
    fun openJD4Config(context: Context) {
        (context as Activity).startActivity(Intent(context, PortableActivity::class.java))
    }

    interface OnUDPHelperListener {

        fun logInfo(msg: String)

        fun result(clientIp: String, clientPort: String, methane: String, co: String, o2: String, temp: String)

    }

}