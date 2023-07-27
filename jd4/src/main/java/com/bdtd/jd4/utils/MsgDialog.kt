package com.bdtd.jd4.utils

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import com.bdtd.jd4.R

class MsgDialog private constructor() {

    companion object {
        val instance: MsgDialog by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            MsgDialog()
        }
    }

    fun show(context: Context, ipAddress: String, port: String) {
        val dialog = Dialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.layout_msg_dialog, null, false)
        dialog.setContentView(view)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        view.findViewById<AppCompatTextView>(R.id.tvIpAddress).text = ipAddress
        view.findViewById<AppCompatTextView>(R.id.tvPort).text = port

        view.findViewById<AppCompatTextView>(R.id.tv_confirm).setOnClickListener(View.OnClickListener {
            dialog.cancel()
        })
    }
}