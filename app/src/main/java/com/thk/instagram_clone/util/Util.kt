package com.thk.instagram_clone.util

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.thk.instagram_clone.R

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

    fun hide() {
        dialog?.hide()
        if (dialog != null) dialog = null
    }
}