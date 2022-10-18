package com.thk.data.util

import android.util.Log
import kotlin.contracts.contract

inline fun <reified T> T.logd(message: String) = Log.d(T::class.java.simpleName, message)