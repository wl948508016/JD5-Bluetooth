package com.bdtd.ccgtest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.bdtd.ccg.CcgHelperKt

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private var btnOpenBluetoothDialog: AppCompatButton? = null
    private var btnOpenForJava: AppCompatButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
    }

    private fun initView() {
        btnOpenBluetoothDialog = findViewById(R.id.btnOpenBluetoothDialog)
        btnOpenForJava = findViewById(R.id.btnOpenForJava)

        btnOpenBluetoothDialog?.setOnClickListener(this)
        btnOpenForJava?.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnOpenBluetoothDialog -> {
//                CcgHelper.getInstance().openBluetoothDialog(this, false, object : CcgHelper.OnCcgHelperListener {
//                    override fun onResultData(methaneVal: String?, co: String?, o2: String?, temp: String?, co2: String?) {
//                        Toast.makeText(this@MainActivity, "methaneVal-->${methaneVal}," +
//                                "co-->${co}," +
//                                "o2-->${o2}," +
//                                "temp-->${temp}," +
//                                "co2-->${co2}", Toast.LENGTH_LONG).show()
//                    }
//                })
                CcgHelperKt.instance.openBluetoothDialog(this, false, object : CcgHelperKt.OnCcgHelperKtListener {
                    override fun onResultData(methaneVal: String, co: String, o2: String, temp: String, co2: String) {
                        Toast.makeText(this@MainActivity, "methaneVal-->${methaneVal}," +
                                "co-->${co}," +
                                "o2-->${o2}," +
                                "temp-->${temp}," +
                                "co2-->${co2}", Toast.LENGTH_LONG).show()
                    }
                })
            }
            R.id.btnOpenForJava -> {
                startActivity(Intent(this, TestActivity::class.java))
            }
        }
    }
}