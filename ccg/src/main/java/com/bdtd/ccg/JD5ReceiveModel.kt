package com.bdtd.ccg

data class JD5ReceiveModel(

    /** 帧头(固定值) */
    val frameHeader: String = "",

    /** 数据长度(固定值) */
    val length: Int = 0,

    /** 时间(yyyy-MM-dd HH:mm:ss) */
    val time: String = "",

    /** 设备唯一码(yyyy-MM-dd HH:mm:ss) */
    val deviceId: String = "",

    /** 甲烷值 */
    val methaneVal: String = "",

    /** 甲烷工作状态  0：故障(false)，1：工作正常(true) */
    val methaneState: Boolean = true,

    /** 氧气值 */
    val o2: String = "",

    /** 氧气工作状态  0：故障(false)，1：工作正常(true) */
    val o2State: Boolean = true,

    /** 一氧化碳值 */
    val co: String = "",

    /** 一氧化碳工作状态  0：故障(false)，1：工作正常(true) */
    val coState: Boolean = true,

    /** 二氧化碳值 */
    val co2: String = "",

    /** 二氧化碳工作状态  0：故障(false)，1：工作正常(true) */
    val co2State: Boolean = true,

    /** 温度值 */
    val temp: String = "",

    /** 温度工作状态  0：故障(false)，1：工作正常(true) */
    val tempState: Boolean = true,

    /** 班次 */
    val classes: String = "",

    /** 用户姓名 */
    val username: String = "",

    /** 电池容量 */
    val batteryCapacity: String = "",

) {

    override fun toString(): String {
        return "JD5ReceiveModel(frameHeader='$frameHeader', length=$length, time='$time', deviceId='$deviceId', methaneVal='$methaneVal', methaneState=$methaneState, o2='$o2', o2State=$o2State, co='$co', coState=$coState, co2='$co2', co2State=$co2State, temp='$temp', tempState=$tempState, classes='$classes', username='$username', batteryCapacity='$batteryCapacity')"
    }
}
