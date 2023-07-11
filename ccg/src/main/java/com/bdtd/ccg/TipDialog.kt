package com.bdtd.ccg

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView

class TipDialog private constructor() {
    private var mListener: OnTipDialogListener? = null

    companion object {
        val instance: TipDialog by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            TipDialog()
        }
    }

    fun show(context: Context, tip: String) {
        val dialog = Dialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.layout_tip_dialog, null, false)

//        val w = (context as Activity).windowManager.defaultDisplay.width
//        view.minimumWidth = (w * 3 / 5) //设置dialog的宽度

        dialog.setContentView(view)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(true)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show()

        val tvTip = view.findViewById<TextView>(R.id.tv_tip)
        val tvCancel = view.findViewById<TextView>(R.id.tv_cancel)
        val tvConfirm = view.findViewById<TextView>(R.id.tv_confirm)

        tvTip.text = tip

        tvCancel.setOnClickListener(View.OnClickListener {
            mListener?.onCancelClick()
            unRegister()
            dialog.cancel()
        })

        tvConfirm.setOnClickListener(View.OnClickListener {
            mListener?.onConfirmClick()
            unRegister()
            dialog.cancel()
        })
    }

    fun setOnTipDialogListener(listener: OnTipDialogListener) {
        mListener = listener
    }

    fun unRegister() {
        mListener = null
    }

    interface OnTipDialogListener {

        fun onCancelClick()

        fun onConfirmClick()

    }
}