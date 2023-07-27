package com.bdtd.jd4

import android.app.Activity
import android.content.Context
import com.bdtd.jd4.common.HexUtil
import com.bdtd.jd4.model.JD4ReceiveModel
import com.bdtd.jd4.utils.JD5ConvertUtils
import java.lang.StringBuilder
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.util.concurrent.Executors

class UDPServer private constructor() {
    private val TAG = "UDPServer"
    private var mContext: Context? = null
    private var mSocket: DatagramSocket? = null
    private var received = true
    private var mListener: OnUDPServerListener? = null

    companion object {
        val instance: UDPServer by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            UDPServer()
        }
    }

    /**
     * 接收客户端发送的数据
     */
    fun receive(context: Context, port: String, listener: OnUDPServerListener) {
        mContext = context
        mListener = listener
        Executors.newSingleThreadExecutor().submit {
            try {
                // 1.创建服务器端DatagramSocket，指定端口
                mSocket = DatagramSocket(port.toInt())
                // 2.创建数据报，用于接收客户端发送的数据
                val data = ByteArray(1024) // 创建字节数组，指定接收的数据包的大小
                val packet = DatagramPacket(data, data.size)
                // 3.接收客户端发送的数据
                log("服务器端已经启动，等待客户端发送数据")
                while (received) {
                    mSocket?.receive(packet) // 此方法在接收到数据报之前会一直阻塞

                    // 4.读取数据
                    val sb = StringBuilder()
                    for (i in 0 until packet.length) {
                        sb.append(String.format("%02X", data[i]))
                    }
                    log("读取到的-->${sb.toString()}")
//                    val resultGb2312 = String(data, 0, packet.length, charset("gb2312"))
//                    log("读取到的gb2312-->${resultGb2312}\nutf-8-->${resultUtf8}\n" + "resultUtf16-->${resultUtf16}\n" + "resultUsAsCII-->${resultUsAsCII}")
                    if (sb.toString().substring(0, 6) == "01041C") {
                        val clientIp = packet.address.toString()
                        val clientPort = packet.port.toString()
                        (mContext as Activity).runOnUiThread {
                            mListener?.onResult(clientIp, clientPort, analyseJD4AutoData(sb.toString()))
                        }
                    }
                }
            } catch (e: Exception) {
                log(e.toString())
            }
        }
    }

    /**
     * 向客户端响应数据
     */
    fun response(socket: DatagramSocket, packet: DatagramPacket) {
        try {
            // 1.定义客户端的地址、端口号、数据
            val address = packet.address
            val port = packet.port
            val data = "I received".toByteArray(charset("gb2312"))
            // 2.创建数据报，包含响应的数据信息
            val newPacket = DatagramPacket(data, data.size, address, port)
            // 3.响应客户端
            socket.send(newPacket)
            // 4.关闭资源
            socket.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun log(msg: String) {
        (mContext as Activity).runOnUiThread {
            mListener?.logInfo(msg)
        }
    }

    /**
     * 关闭UDP服务端
     */
    fun closeUDP() {
        received = false
        mSocket?.close()
        mContext = null
        mListener = null
        mSocket = null
    }

    /**
     * 解析JD4自动上传的数据
     */
    private fun analyseJD4AutoData(hexStr: String): JD4ReceiveModel {
//        val data = "01042204421041000C00000000EA61000000010000000100C90001016D00010025006200C8FEE4";
//        if (ProtocolUtil.checkCrc16(hexStr)) {
            val hexArr = HexUtil.stringToHexArray(hexStr)
            return JD4ReceiveModel(
                frameHeader = hexArr[0] + hexArr[1],
                length = hexArr[2].toInt(16),
                time = JD5ConvertUtils.instance.getTime(hexArr[3] + hexArr[4] + hexArr[5] + hexArr[6]),
                deviceId = "", // 7-12暂不解析
                methaneVal = ((hexArr[13] + hexArr[14]).toInt(16).toFloat() / 100).toString(), // 甲烷当前值(原值放大了100倍)
                methaneState = JD5ConvertUtils.instance.getPartStatus(hexArr[15] + hexArr[16]) == 1, // 甲烷检测元件工作状态(0：故障，1：工作正常)
                co = (hexArr[17] + hexArr[18]).toInt(16).toString(), // 一氧化碳当前值(原值放大了1倍)
                coState = JD5ConvertUtils.instance.getPartStatus(hexArr[19] + hexArr[20]) == 1, // 一氧化碳检测元件工作状态(0：故障，1：工作正常)
                o2 = ((hexArr[21] + hexArr[22]).toInt(16).toFloat() / 10).toString(), // 氧气当前值(原值放大了10倍)
                o2State = JD5ConvertUtils.instance.getPartStatus(hexArr[22] + hexArr[23]) == 1,  // 氧气检测元件工作状态(0：故障，1：工作正常)
                temp = ((hexArr[24] + hexArr[25]).toInt(16).toFloat() / 10).toString(), // 温度当前值(原值放大了10倍)
                tempState = JD5ConvertUtils.instance.getPartStatus(hexArr[26] + hexArr[27]) == 1, // 温度检测元件工作状态(0：故障，1：工作正常)
                batteryCapacity = "", // 28-29暂不解析
            )
//        } else {
//            return JD4ReceiveModel()
//        }
    }

    interface OnUDPServerListener {

        fun logInfo(msg: String)

        fun onResult(clientIp: String, clientPort: String, jd4Data: JD4ReceiveModel)

    }
}