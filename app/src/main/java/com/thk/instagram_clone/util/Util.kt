package com.thk.instagram_clone.util

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import com.thk.instagram_clone.R

/**
 * 전역적으로 사용하기 위한 Loading Dialog.
 * 사용 시 추가된 화면의 context를 가지기 때문에 dismiss할 때 null처리를 해줌.
 */
object LoadingDialog {
    private var dialog: Dialog? = null

    val isShowing
        get() = dialog?.isShowing == true

    fun show(context: Context) {
        if (dialog == null) {
            dialog = Dialog(context).apply {
                setContentView(R.layout.dialog_loading)
                setCancelable(false)
                setCanceledOnTouchOutside(false)
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
        }
        dialog?.show()
    }

    fun dismiss() {
        dialog?.dismiss()
        dialog = null
    }
}


inline fun <reified T> T.logd(message: String) = Log.d(T::class.java.simpleName, message)