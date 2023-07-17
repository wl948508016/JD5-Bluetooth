package com.bdtd.ccg.utils

/**
 * JD5数据转换
 */

class JD5ConvertUtils private constructor() {

    companion object {
        val instance: JD5ConvertUtils by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            JD5ConvertUtils()
        }
    }

    /**
     * 获取日期数据
     *
     * 十进制字符串  转  标准日期字符串(格式：yyyy-MM-dd HH:mm:ss)
     */

    fun getTime(hexTime: String): String {
        var binaryTime = hexTime.toInt(16).toString(2) // 二进制字符串

        // 长度不足32时，在前面补0
        if (binaryTime.length < 32) {
            binaryTime = makeUpZero(binaryTime, 32)
        }

        // bit26~31，年，0~64，2020年开始的年份。6个位
        val year = (2020 + binaryTime.substring(0, 6).toInt(2)).toString()
        // bit22~25，月，1~12。4个位
        val month = binaryTime.substring(6, 10).toInt(2).toString()
        // bit17~21，日，1~31。5个位
        val day = binaryTime.substring(10, 15).toInt(2).toString()
        // bit12~16，时，0~23。5个位
        val hour = binaryTime.substring(15, 20).toInt(2).toString()
        // bit6~11，分，0~59。6个位
        val minute = binaryTime.substring(20, 26).toInt(2).toString()
        // bit0~5，秒，0~59。6个位
        val second = binaryTime.substring(26, 32).toInt(2).toString()

        val monthStr = if (month.length == 1) "0$month" else month
        val dayStr = if (day.length == 1) "0$day" else day
        val hourStr = if (hour.length == 1) "0$hour" else hour
        val minuteStr = if (minute.length == 1) "0$minute" else minute
        val secondStr = if (second.length == 1) "0$second" else second
        return "$year-$monthStr-$dayStr $hourStr:$minuteStr:$secondStr"
    }

    /**
     * 便携仪mac地址
     */
    fun getMac(macComplete: String): String {
        return macComplete.toInt(16).toString(16).uppercase() // 去掉十六进制字符串前面所有的0
    }

    /**
     * 便携仪检测元件的工作状态
     *
     * 十六进制字符串  转  状态编号
     *
     * bit0~4(5个位)：工作状态：0：故障，1：工作正常
     * bit5~15(11个位)：保留
     */
    fun getPartStatus(hexStatus: String): Int {
        var binaryStatus = hexStatus.toInt(16).toString(2) // 二进制字符串
        // 长度不足16时，在前面补0
        if (binaryStatus.length < 16) {
            binaryStatus = makeUpZero(binaryStatus, 16)
        }
        return binaryStatus.substring(11, 16).toInt(2) // 截取工作状态
    }

    /**
     * 用户名
     *
     * 最长8个字节、4个汉字，不足8个字节补空格(0x20)
     * 例：0xD5, 0xC5, 0xC8, 0xFD, 0x20, 0x20, 0x20, 0x20
     */
    fun getUsername(vararg hexs: String): String {
        val byteArr: MutableList<Byte> = mutableListOf()
        var i = 0
        while (hexs[i] != "20") {
            // 先将十六进制转化为十进制，再转化为字节
            // 两个十六进制字符为一组，一组是一个字节
            val byte = hexs[i].toInt(16).toByte()
            byteArr.add(byte)
            i++
        }
        return String(byteArr.toByteArray(), charset("gb2312"))
    }

    /**
     * 长度不足的二进制数，在前面补0
     */
    private fun makeUpZero(originStr: String, length: Int): String {
        val sb = StringBuilder()
        for (i in 1 .. (length - originStr.length)) {
            sb.append("0")
        }
        return sb.toString() + originStr
    }

}