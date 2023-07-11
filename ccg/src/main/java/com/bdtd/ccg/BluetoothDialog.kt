package com.bdtd.ccg

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bdtd.ccg.common.HexUtil
import com.bdtd.ccg.common.ProtocolUtil

class BluetoothDialog private constructor() {
    private val TAG = "BluetoothDialog"
    private var mContext: Context? = null
    private var dialog: Dialog? = null
    private var adapter: BluetoothDevAdapter? = null

    private var tvInfo: AppCompatTextView? = null
    private var tvData: AppCompatTextView? = null
    private var tvWriteData: AppCompatTextView? = null
    private var connected = false // 是否处于已连接状态
    private var checkSuccess = false // 校验成功
    private var curDev: BluetoothDevModel? = null
    private var bluetoothUtils: BluetoothClientUtils? = null
    private var curData: JD5ReceiveModel? = null
    private var autoRead: Boolean = false // true表示主动读取JD5数据

    private var mListener: JD5DialogListener? = null

    companion object {
        val instance: BluetoothDialog by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            BluetoothDialog()
        }
    }

    /**
     * autoRead == true : APP主动向JD5发送读取指令
     * autoRead == false : APP不主动向JD5发送读取指令
     */
    fun show(context: Context, utils: BluetoothClientUtils?, autoRead: Boolean) {
        mContext = context
        bluetoothUtils = utils
        if (dialog != null && dialog!!.isShowing) {
            // 当上一个dialog未消失，先将上一个dialog取消
            dialog?.cancel()
        }

        initBluetoothListener()
        this.autoRead = autoRead

        dialog = Dialog(mContext!!)
        val view = LayoutInflater.from(mContext).inflate(R.layout.layout_bluetooth_dialog, null, false)
        dialog?.setContentView(view)
        dialog?.setCancelable(false)
        dialog?.setCanceledOnTouchOutside(true)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val ivSearch = view.findViewById<AppCompatImageView>(R.id.iv_search)
        tvInfo = view.findViewById<AppCompatTextView>(R.id.tv_info)
        tvData = view.findViewById<AppCompatTextView>(R.id.tv_data)
        tvWriteData = view.findViewById<AppCompatTextView>(R.id.tv_write_data)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)

        adapter = BluetoothDevAdapter()
        val layoutManager = LinearLayoutManager(mContext)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        tvWriteData?.visibility = if (this.autoRead) View.GONE else View.VISIBLE

        dialog?.show()

        ivSearch.setOnClickListener(View.OnClickListener {
            tvInfo?.text = "开始扫描..."
            adapter?.clear() // 先从列表中将所有设备清除
            getConnectedDev() // 再将已连接的设备加进列表中
            bluetoothUtils?.scanDev() // 然后扫描
        })

        tvWriteData?.setOnClickListener(View.OnClickListener {
            if (connected) {
                bluetoothUtils?.writeToJD5("01420010A0")
            }
        })

        adapter?.setListener(object : BluetoothDevAdapter.OnBluetoothDevAdapterListener {
            override fun onItemClick(dev: BluetoothDevModel) {
                connectDev(dev)
            }
        })
    }

    private fun initBluetoothListener() {
        bluetoothUtils?.setConnectListener(object : BluetoothClientUtils.BluetoothConnectListener {
            override fun scanResult(dev: BluetoothDevModel) {
                adapter?.addData(dev)
            }

            override fun scanStop() {
                tvInfo?.text = "扫描结束"
            }

            override fun connected() {
                connected = true
                curDev?.connected = true
                (mContext as Activity).runOnUiThread {
                    if (autoRead) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            bluetoothUtils?.writeToJD5("01420010A0")
                        }, 200)
                    } else {
                        textViewClickEnable(tvWriteData!!, true)
                    }
                    adapter?.notifyDataSetChanged()
                }
            }

            override fun disConnect() {
                connected = false
                curDev?.connected = false
                (mContext as Activity).runOnUiThread {
                    if (curDev != null) adapter?.removeData(curDev!!)
                    curDev = null
                }
            }

            override fun logInfo(msg: String) {
                var info = ""
                if (msg.contains("257")) { info = "${msg}(短时间内连接次数过多)" } else { info = msg }
                tvInfo?.text = info
            }
        })

        bluetoothUtils?.setCommListener(object : BluetoothClientUtils.BluetoothCommListener {
            override fun readResult(data: String) {
            }

            override fun writeSuccess() {
                (mContext as Activity).runOnUiThread {
                    tvInfo?.text = "等待回应..."
                }
            }

            override fun goBackResult(data: String) {
            }

            override fun jd5AutoGasData(data: String) {
                (mContext as Activity).runOnUiThread {
                    try {
                        if (ProtocolUtil.checkCrc16(data)) {
                            checkSuccess = true
                            curData = analyseAutoData(data)
                            tvData?.text = data
                            fillData()
                        } else {
                            tvData?.text = "CRC16校验失败"
                        }
                    } catch (e: Exception) {
                        tvData?.text = "错误的数据-->${data}长度-->${data.length}"
                    }
                }
            }

            @SuppressLint("SetTextI18n")
            override fun jd5HandGasData(data: String) {
                (mContext as Activity).runOnUiThread {
                    try {
                        if (ProtocolUtil.checkCrc16(data)) {
                            checkSuccess = true
                            curData = analyseHandData(data)
                            tvData?.text = data
                            fillData()
//                            textViewClickEnable(tvFillData!!, true)
                        } else {
                            tvData?.text = "CRC16校验失败"
                        }
                    } catch (e: Exception) {
                        tvData?.text = "错误的数据-->${data}长度-->${data.length}"
                    }
                }
            }

        })
    }

    private fun textViewClickEnable(textView: AppCompatTextView, enable: Boolean) {
        if (enable) {
            textView.setTextColor(Color.parseColor("#ffffff"))
            textView.setBackgroundResource(R.drawable.bg_blue_bluetooth_button)
        } else {
            textView.setTextColor(Color.parseColor("#FFD3D3D3"))
            textView.setBackgroundResource(R.drawable.bg_white_bluetooth_button)
        }
    }

    /**
     * 填充数据
     */
    private fun fillData() {
        if (connected && checkSuccess) {
            mListener?.fillData(curData!!)
            dialog?.cancel()
        }
    }

    /**
     * 解析JD5手动上传的数据
     */
    fun analyseHandData(hexStr: String): JD5ReceiveModel {
        val hexArr = HexUtil.stringToHexArray(hexStr)
        return JD5ReceiveModel(
            frameHeader = hexArr[0] + hexArr[1],
            length = hexArr[2].toInt(16),
            time = JD5ConvertUtils.instance.getTime(hexArr[3] + hexArr[4] + hexArr[5] + hexArr[6]),
            deviceId = hexArr[7] + hexArr[8] + hexArr[9] + hexArr[10] + hexArr[11] + hexArr[12],
            methaneVal = ((hexArr[13] + hexArr[14]).toInt(16).toFloat() / 100).toString(), // 甲烷当前值(原值放大了100倍)
            methaneState = JD5ConvertUtils.instance.getPartStatus(hexArr[15] + hexArr[16]) == 1, // 甲烷检测元件工作状态(0：故障，1：工作正常)
            o2 = ((hexArr[17] + hexArr[18]).toInt(16).toFloat() / 10).toString(), // 氧气当前值(原值放大了10倍)
            o2State = JD5ConvertUtils.instance.getPartStatus(hexArr[19] + hexArr[20]) == 1,  // 氧气检测元件工作状态(0：故障，1：工作正常)
            co = (hexArr[21] + hexArr[22]).toInt(16).toString(), // 一氧化碳当前值(原值放大了1倍)
            coState = JD5ConvertUtils.instance.getPartStatus(hexArr[23] + hexArr[24]) == 1, // 一氧化碳检测元件工作状态(0：故障，1：工作正常)
            co2 = ((hexArr[25] + hexArr[26]).toInt(16).toFloat() / 100).toString(), // 二氧化碳当前值(原值放大了100倍)
            co2State = JD5ConvertUtils.instance.getPartStatus(hexArr[27] + hexArr[28]) == 1, // 二氧化碳检测元件工作状态(0：故障，1：工作正常)
            temp = ((hexArr[29] + hexArr[30]).toInt(16).toFloat() / 10).toString(), // 温度当前值(原值放大了10倍)
            tempState = JD5ConvertUtils.instance.getPartStatus(hexArr[31] + hexArr[32]) == 1, // 温度检测元件工作状态(0：故障，1：工作正常)
            classes = (hexArr[33] + hexArr[34]).toInt(16).toString(), // 班次
            username = JD5ConvertUtils.instance.getUsername(hexArr[35], hexArr[36], hexArr[37], hexArr[38], hexArr[39], hexArr[40], hexArr[41], hexArr[42]) // 用户名
        )
    }

    /**
     * 解析JD5自动上传的数据
     */
    fun analyseAutoData(hexStr: String): JD5ReceiveModel {
        val hexArr = HexUtil.stringToHexArray(hexStr)
        return JD5ReceiveModel(
            frameHeader = hexArr[0] + hexArr[1],
            length = hexArr[2].toInt(16),
            time = JD5ConvertUtils.instance.getTime(hexArr[3] + hexArr[4] + hexArr[5] + hexArr[6]),
            deviceId = hexArr[7] + hexArr[8] + hexArr[9] + hexArr[10] + hexArr[11] + hexArr[12],
            methaneVal = ((hexArr[13] + hexArr[14]).toInt(16).toFloat() / 100).toString(), // 甲烷当前值(原值放大了100倍)
            methaneState = JD5ConvertUtils.instance.getPartStatus(hexArr[15] + hexArr[16]) == 1, // 甲烷检测元件工作状态(0：故障，1：工作正常)
            o2 = ((hexArr[17] + hexArr[18]).toInt(16).toFloat() / 10).toString(), // 氧气当前值(原值放大了10倍)
            o2State = JD5ConvertUtils.instance.getPartStatus(hexArr[19] + hexArr[20]) == 1,  // 氧气检测元件工作状态(0：故障，1：工作正常)
            co = (hexArr[21] + hexArr[22]).toInt(16).toString(), // 一氧化碳当前值(原值放大了1倍)
            coState = JD5ConvertUtils.instance.getPartStatus(hexArr[23] + hexArr[24]) == 1, // 一氧化碳检测元件工作状态(0：故障，1：工作正常)
            co2 = ((hexArr[25] + hexArr[26]).toInt(16).toFloat() / 100).toString(), // 二氧化碳当前值(原值放大了100倍)
            co2State = JD5ConvertUtils.instance.getPartStatus(hexArr[27] + hexArr[28]) == 1, // 二氧化碳检测元件工作状态(0：故障，1：工作正常)
            temp = ((hexArr[29] + hexArr[30]).toInt(16).toFloat() / 10).toString(), // 温度当前值(原值放大了10倍)
            tempState = JD5ConvertUtils.instance.getPartStatus(hexArr[31] + hexArr[32]) == 1, // 温度检测元件工作状态(0：故障，1：工作正常)
            batteryCapacity = ((hexArr[33] + hexArr[34]).toInt(16)).toString() // 电池容量
        )
    }

    fun getConnectedDev() {
        val deviceList = bluetoothUtils?.getConnectedDevice()
        if (deviceList != null && deviceList.isNotEmpty()) {
            adapter?.addData(deviceList)
        }
    }

    @SuppressLint("MissingPermission")
    fun connectNfc(device: BluetoothDevice) {
        val devModel = BluetoothDevModel(device)
        adapter?.addData(devModel)
        adapter?.notifyDataSetChanged()
        connectDev(devModel)
    }

    private fun connectDev(dev: BluetoothDevModel) {
        connected = false
        checkSuccess = false
        tvInfo?.text = "正在连接..."
        adapter?.resetData()
//        textViewClickEnable(tvFillData!!, false)
        curDev = dev
        bluetoothUtils?.connect(dev, true)
    }

    fun release() {
        adapter?.release()
        adapter = null
        dialog?.cancel()
        dialog = null
        adapter = null
        mContext = null
        tvData = null
        tvInfo = null
        tvWriteData = null
        mListener = null
    }

    fun setJD5DialogListener(listener: JD5DialogListener) {
        mListener = listener
    }

    interface JD5DialogListener {

        fun fillData(data: JD5ReceiveModel)

    }

}