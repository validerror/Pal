package com.linmu.pal.ui

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.BatteryManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.imageview.ShapeableImageView
import com.linmu.pal.DataHolder
import com.linmu.pal.R
import com.linmu.pal.broadcasts.BatteryChargeReceiver
import com.linmu.pal.entity.MediaInfo
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class PalActivity : AppCompatActivity() {
    private val TAG: String = "Activity_Pal"

    private var startColor: Int = 0xFFFF0000.toInt()
    private var endColor: Int = 0xFF00FF00.toInt()
    private var targetOrientation: Int = 0
    private lateinit var decorView: ViewGroup
    private lateinit var widgetsVG: ViewGroup
    private lateinit var timerTV: TextView
    private lateinit var batterySIV: ShapeableImageView
    private lateinit var chargeIV: ImageView
    private lateinit var destinationDir: File
    private lateinit var displayMediaInfo: MediaInfo
    private lateinit var batteryManager: BatteryManager
    private var imageBitmap: Bitmap? = null
    private var mediaIndex: Int = 0
    private var modifiedVV: ModifiedVideoVIew? = null
    private val handler = Handler(Looper.getMainLooper())
    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            timerTV.text = getLocalTime()
            handler.postDelayed(this, 5000)
        }
    }
    private val updateBatteryInfoRunnable = object : Runnable {
        override fun run() {
            // battery level
            val nowBatteryLevel =
                batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            val displayColor = getGradientColor(nowBatteryLevel, startColor, endColor)
            batterySIV.setImageDrawable(ColorDrawable(displayColor))
            handler.postDelayed(this, 5000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // set orientation
        setTheme(R.style.RealFullScreenTheme)
        mediaIndex = intent.getIntExtra("MediaIndex", 0)
        displayMediaInfo = DataHolder.mediaList[mediaIndex]
        startColor = displayMediaInfo.batteryBarStartColor
        endColor = displayMediaInfo.batteryBarEndColor
        targetOrientation = displayMediaInfo.orientation
        requestedOrientation = when (targetOrientation) {
            MediaInfo.ORIENTATION_PORTRAIT -> {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }

            else -> {
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
        }
        // set layout
        decorView = window.decorView as ViewGroup
        // hide the navigation bar
        val insetsController = window.insetsController
        insetsController?.hide(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE)
        // screen keep on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        // get service
        batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val batteryFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryChargeReceiver = BatteryChargeReceiver(
            {chargeIV.visibility = View.VISIBLE},
            {chargeIV.visibility = View.INVISIBLE}
        )
        registerReceiver(batteryChargeReceiver, batteryFilter)
        // set media
        destinationDir = File(filesDir, "media")
        when (displayMediaInfo.type) {
            MediaInfo.TYPE_VIDEO -> {
                setupModifiedVideoView()
            }

            else -> {
                setupImageView()
            }
        }
        setContentView(R.layout.activity_pal)
        widgetsVG = findViewById(R.id.pa_frameLayout)
        setupWidgets()
    }

    override fun onStart() {
        super.onStart()
        modifiedVV?.start()
    }

    private fun setupWidgets() {
        when (targetOrientation) {
            MediaInfo.ORIENTATION_PORTRAIT -> {
                elementGravityPortrait(widgetsVG, displayMediaInfo.gravity)
            }

            else -> {
                elementGravityLandscape(widgetsVG, displayMediaInfo.gravity)
            }
        }

        setupTimer()
        setupBatteryInfo()

    }

    private fun setupBatteryInfo() {
        batterySIV = widgetsVG.findViewById(R.id.pa_siv_battery)
        chargeIV = widgetsVG.findViewById(R.id.pa_iv_charge)
        if (displayMediaInfo.enableBatteryInfo) {
            handler.post(updateBatteryInfoRunnable)
        } else {
            batterySIV.visibility = View.GONE
            chargeIV.visibility = View.GONE
        }
    }

    private fun setupTimer() {
        timerTV = widgetsVG.findViewById(R.id.pa_tv_timer)
        if (displayMediaInfo.enableClock) {
            val typeface: Typeface = Typeface.createFromAsset(assets, "fonts/digital-7.ttf")
            timerTV.setTextColor(displayMediaInfo.clockColor)
            timerTV.typeface = typeface
            handler.post(updateTimeRunnable)
        } else {
            timerTV.visibility = View.GONE
        }
    }

    private fun elementGravityPortrait(view: View, gravityIndex: Int) {
        var rotationAngle = 0F
        val layoutParams = view.layoutParams as FrameLayout.LayoutParams
        when (gravityIndex) {
            MediaInfo.GRAVITY_START -> {
                layoutParams.gravity = Gravity.START or Gravity.CENTER_VERTICAL
                layoutParams.setMargins(-150, 0, 0, 0)
                rotationAngle = 90F
            }

            MediaInfo.GRAVITY_TOP -> {
                layoutParams.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                layoutParams.setMargins(0, 50, 0, 0)
                rotationAngle = 180F
            }

            MediaInfo.GRAVITY_END -> {
                layoutParams.gravity = Gravity.END or Gravity.CENTER_VERTICAL
                layoutParams.setMargins(0, 0, -150, 0)
                rotationAngle = 270F
            }

            else -> {
                layoutParams.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                layoutParams.setMargins(0, 0, 0, 50)
                rotationAngle = 0F
            }
        }
        view.layoutParams = layoutParams
        view.rotation = rotationAngle
    }

    private fun elementGravityLandscape(view: View, gravityIndex: Int) {
        var rotationAngle = 0F
        val layoutParams = view.layoutParams as FrameLayout.LayoutParams
        when (gravityIndex) {
            MediaInfo.GRAVITY_START -> {
                layoutParams.gravity = Gravity.START or Gravity.CENTER_VERTICAL
                layoutParams.setMargins(-150, 0, 0, 0)
                rotationAngle = 90F
            }

            MediaInfo.GRAVITY_TOP -> {
                layoutParams.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                layoutParams.setMargins(0, 50, 0, 0)
                rotationAngle = 180F
            }

            MediaInfo.GRAVITY_END -> {
                layoutParams.gravity = Gravity.END or Gravity.CENTER_VERTICAL
                layoutParams.setMargins(0, 0, -150, 0)
                rotationAngle = 270F
            }

            else -> {
                layoutParams.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                layoutParams.setMargins(0, 0, 0, 50)
                rotationAngle = 0F
            }
        }
        view.layoutParams = layoutParams
        view.rotation = rotationAngle
    }

    override fun onDestroy() {
        super.onDestroy()
        imageBitmap?.recycle()
        handler.removeCallbacks(updateTimeRunnable)
        handler.removeCallbacks(updateBatteryInfoRunnable)
        modifiedVV?.stopPlayback()
    }

    private fun setupModifiedVideoView() {
        modifiedVV = ModifiedVideoVIew(this)
        modifiedVV?.let {
            it.focusable = View.NOT_FOCUSABLE
            val layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            layoutParams.gravity = Gravity.CENTER
            it.layoutParams = layoutParams
            decorView.addView(modifiedVV, 0)
            val videoFileName = displayMediaInfo.mediaName
            val videoFile = File(destinationDir, videoFileName)
            it.setVideoPath(videoFile.absolutePath)
            it.setOnCompletionListener {
                it.seekTo(0)
                it.start()
            }
        }

    }

    private fun setupImageView() {
        val imageFileName: String = displayMediaInfo.mediaName
        val imageFile = File(destinationDir, imageFileName)
        imageBitmap = getBitmapFromFile(imageFile)
        imageBitmap?.let { bmp ->
            val imageView = ImageView(this)
            imageView.focusable = View.NOT_FOCUSABLE
            imageView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.setImageBitmap(bmp)
            decorView.addView(imageView, 0)
        }
    }

    private fun getBitmapFromFile(file: File): Bitmap? {
        return try {
            FileInputStream(file).use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun getLocalTime(): String {
        val currentTime = LocalTime.now()
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        return currentTime.format(formatter)
    }

    fun getGradientColor(i: Int, startColor: Int, endColor: Int): Int {
        val clampedI = i.toFloat() / 100
        val startAlpha = (startColor shr 24) and 0xFF
        val startRed = (startColor shr 16) and 0xFF
        val startGreen = (startColor shr 8) and 0xFF
        val startBlue = startColor and 0xFF

        val endAlpha = (endColor shr 24) and 0xFF
        val endRed = (endColor shr 16) and 0xFF
        val endGreen = (endColor shr 8) and 0xFF
        val endBlue = endColor and 0xFF

        val red = (startRed + (endRed - startRed) * clampedI).toInt()
        val green = (startGreen + (endGreen - startGreen) * clampedI).toInt()
        val blue = (startBlue + (endBlue - startBlue) * clampedI).toInt()
        val alpha = (startAlpha + (endAlpha - startAlpha) * clampedI).toInt()

        return (alpha shl 24) or (red shl 16) or (green shl 8) or blue
    }
}