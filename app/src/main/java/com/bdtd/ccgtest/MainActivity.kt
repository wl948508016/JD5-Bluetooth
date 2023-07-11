package com.bdtd.ccgtest

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.bdtd.ccg.CcgHelper
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions

class MainActivity : AppCompatActivity(), View.OnClickListener {
    /**
     * 需要进行检测的权限数组
     */
    private val needPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION, // BLE蓝牙需要获取位置权限
        Manifest.permission.ACCESS_COARSE_LOCATION, // BLE蓝牙需要获取位置权限
        Manifest.permission.BLUETOOTH_CONNECT, // HONOR60_LSA_AN00需要动态设置此权限(其他设备不需要此权限)
        Manifest.permission.BLUETOOTH_SCAN, // HONOR60_LSA_AN00需要动态设置此权限(其他设备不需要此权限)
        Manifest.permission.BLUETOOTH_ADVERTISE // HONOR60_LSA_AN00需要动态设置此权限(其他设备不需要此权限)
    )

    private var btnOpenBluetoothDialog: AppCompatButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermission()
    }

    private fun initView() {
        btnOpenBluetoothDialog = findViewById(R.id.btnOpenBluetoothDialog)

        btnOpenBluetoothDialog?.setOnClickListener(this)
    }

    private fun requestPermission() {
        XXPermissions.with(this) // 申请多个权限
            .permission(needPermissions) // 设置权限请求拦截器（局部设置）
            //.interceptor(new PermissionInterceptor())
            // 设置不触发错误检测机制（局部设置）
            //.unchecked()
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: List<String>, all: Boolean) {
                    initView()
                }

                override fun onDenied(permissions: List<String>, never: Boolean) {
                    showMissingPermissionDialog()
                }
            })
    }

    /**
     * 显示提示信息
     *
     * @since 2.5.0
     */
    private fun showMissingPermissionDialog() {
        try {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("提示")
            builder.setMessage("当前应用缺少必要权限。\\n\\n请点击\\\"设置\\\"-\\\"权限\\\"-打开所需权限")
            // 拒绝, 退出应用
            builder.setNegativeButton(
                "取消"
            ) { _, _ ->
                try {
                    Toast.makeText(this, "缺少必要权限", Toast.LENGTH_SHORT).show()
                    //finish();
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
            builder.setPositiveButton(
                "设置"
            ) { _, _ ->
                try {
                    startAppSettings()
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
    private fun startAppSettings() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnOpenBluetoothDialog -> {
                CcgHelper.openBluetoothDialog(this, false)
            }
        }
    }
}