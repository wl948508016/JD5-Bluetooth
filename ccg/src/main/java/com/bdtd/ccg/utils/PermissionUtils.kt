package com.bdtd.ccg.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions

class PermissionUtils private constructor() {

    /**
     * 需要进行检测的权限数组
     */
    private val needPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION, // BLE蓝牙需要获取位置权限
        Manifest.permission.ACCESS_COARSE_LOCATION, // BLE蓝牙需要获取位置权限
//        Manifest.permission.BLUETOOTH_CONNECT, // HONOR60_LSA_AN00需要动态设置此权限(其他设备不需要此权限)
//        Manifest.permission.BLUETOOTH_SCAN, // HONOR60_LSA_AN00需要动态设置此权限(其他设备不需要此权限)
//        Manifest.permission.BLUETOOTH_ADVERTISE // HONOR60_LSA_AN00需要动态设置此权限(其他设备不需要此权限)
    )

    private var mListener: OnPermissionUtilsListener? = null

    companion object {
        val instance: PermissionUtils by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            PermissionUtils()
        }
    }

    fun requestPermission(context: Context, listener: OnPermissionUtilsListener) {
        mListener = listener
        XXPermissions.with(context) // 申请多个权限
            .permission(needPermissions) // 设置权限请求拦截器（局部设置）
            //.interceptor(new PermissionInterceptor())
            // 设置不触发错误检测机制（局部设置）
            //.unchecked()
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: List<String>, all: Boolean) {
                    mListener?.onPermissionSuccess()
                    mListener = null
                }

                override fun onDenied(permissions: List<String>, never: Boolean) {
                    mListener = null
                    showMissingPermissionDialog(context)
                }
            })
    }

    /**
     * 显示提示信息
     *
     * @since 2.5.0
     */
    private fun showMissingPermissionDialog(context: Context) {
        try {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("提示")
            builder.setMessage("当前应用缺少必要权限。\\n\\n请点击\\\"设置\\\"-\\\"权限\\\"-打开所需权限")
            // 拒绝, 退出应用
            builder.setNegativeButton(
                "取消"
            ) { _, _ ->
                try {
                    Toast.makeText(context, "缺少必要权限", Toast.LENGTH_SHORT).show()
                    //finish();
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
            builder.setPositiveButton(
                "设置"
            ) { _, _ ->
                try {
                    startAppSettings(context)
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
            builder.setCancelable(false)
            builder.show()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    /**
     * 启动应用的设置
     *
     * @since 2.5.0
     */
    private fun startAppSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:${context.packageName}")
            (context as Activity).startActivity(intent)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    interface OnPermissionUtilsListener {

        fun onPermissionSuccess()

    }
}