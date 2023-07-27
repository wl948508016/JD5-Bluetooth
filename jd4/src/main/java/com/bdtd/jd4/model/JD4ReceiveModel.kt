package com.bdtd.jd4.model

data class JD4ReceiveModel(
    /** 帧头(固定值) */
    val frameHeader: String = "",

    /** 数据长度(固定值) */
    val length: Int = 0,

    /** 时间(yyyy-MM-dd HH:mm:ss) */
    val time: String = "",

    /** 设备唯一编号 */
    val deviceId: String = "",

    /** 甲烷值 */
    val methaneVal: String = "",

    /** 甲烷工作状态  0：故障(false)，1：工作正常(true) */
    val methaneState: Boolean = true,

    /** 一氧化碳值 */
    val co: String = "",

    /** 一氧化碳工作状态  0：故障(false)，1：工作正常(true) */
    val coState: Boolean = true,

    /** 氧气值 */
    val o2: String = "",

    /** 氧气工作状态  0：故障(false)，1：工作正常(true) */
    val o2State: Boolean = true,

    /** 温度值 */
    val temp: String = "",

    /** 温度工作状态  0：故障(false)，1：工作正常(true) */
    val tempState: Boolean = true,

    /** 电池容量 */
    val batteryCapacity: String = "",

    /** 校验码是否正确 */
    val crc16: Boolean = true,
) {

}