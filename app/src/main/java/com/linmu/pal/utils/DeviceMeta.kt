package com.linmu.pal.utils

import android.content.Context
import android.view.WindowManager

class DeviceMeta {
    companion object {
        fun getMaxBounds(context: Context):Pair<Int,Int>{
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val windowMetric = windowManager.maximumWindowMetrics
            return Pair(windowMetric.bounds.width(),windowMetric.bounds.height())
        }
        fun getAvailableBounds(context: Context):Pair<Int,Int>{
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val windowMetric = windowManager.currentWindowMetrics
            return Pair(windowMetric.bounds.width(),windowMetric.bounds.height())
        }
    }
}