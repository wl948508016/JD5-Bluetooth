package com.bdtd.jd4

import android.util.Log
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.Executors

class UDPClient private constructor() {
    private val TAG = "UDPClient"

    companion object {
        val instance: UDPClient by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            UDPClient()
        }
    }

    /**
     * 向服务端发送数据
     */
    fun send(port: String) {
        Executors.newSingleThreadExecutor().submit {
            try {
                // 1.定义服务端的地址、端口号、数据
                val address = InetAddress.getByName("192.168.124.45")
                val data = "01041C0442104100000000EA61000000010000000100C90001016D00010062FB0D".toByteArray(charset("gb2312"))
                // 2.创建数据报，包含发送的数据信息
                val packet = DatagramPacket(data, data.size, address, port.toInt())
                // 3.创建DatagramSocket对象
                val socket = DatagramSocket()
                // 4.向服务器端发送数据报
                socket.send(packet)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 接收服务端相应的数据
     */
    fun receive(socket: DatagramSocket) {
        // 1.创建数据报，用于接收服务器端响应的数据
        val data = ByteArray(1024)
        val packet = DatagramPacket(data, data.size)
        // 2.接收服务器响应的数据
        socket.receive(packet)
        // 3.读取数据
        val response = String(data, 0, packet.length)
        Log.i("main","接收到服务端的数据-->$response")
        // 4.关闭资源
        socket.close()
    }
}