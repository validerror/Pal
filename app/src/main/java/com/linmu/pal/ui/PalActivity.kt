package com.linmu.pal.ui

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
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
import com.linmu.pal.DataHolder
import com.linmu.pal.R
import com.linmu.pal.entity.MediaInfo
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class PalActivity : AppCompatActivity() {
    private val TAG:String = "Activity_Pal"

    private var targetOrientation:Int = 0
    private lateinit var decorView: ViewGroup
    private lateinit var timerTV: TextView
    private lateinit var destinationDir: File
    private lateinit var displayMediaInfo:MediaInfo
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // set orientation
        setTheme(R.style.RealFullScreenTheme)
        mediaIndex = intent.getIntExtra("MediaIndex", 0)
        displayMediaInfo = DataHolder.mediaList[mediaIndex]
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
        // bind the elements in layout and set the digital font
        if (displayMediaInfo.enableClock){
            setupTimer()
        }else{
            timerTV = findViewById(R.id.pa_tv_timer)
            timerTV.visibility = View.GONE
        }
    }

    override fun onStart() {
        super.onStart()
        modifiedVV?.start()
    }

    private fun setupTimer(){
        timerTV = findViewById(R.id.pa_tv_timer)
        val typeface: Typeface = Typeface.createFromAsset(assets, "fonts/digital-7.ttf")
        timerTV.setTextColor(displayMediaInfo.clockColor)
        timerTV.typeface = typeface
        // start timer
        handler.post(updateTimeRunnable)
        // timer gravity
        when(targetOrientation){
            MediaInfo.ORIENTATION_PORTRAIT -> {
                elementGravityPortrait(timerTV, displayMediaInfo.gravity)
            }
            else -> {
                elementGravityLandscape(timerTV,displayMediaInfo.gravity)
            }
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

    private fun elementGravityLandscape(view: View,gravityIndex: Int){
        var rotationAngle = 0F
        val layoutParams = view.layoutParams as FrameLayout.LayoutParams
        when (gravityIndex) {
            MediaInfo.GRAVITY_START -> {
                layoutParams.gravity = Gravity.CENTER
                layoutParams.setMargins(0, 0, 0, 0)
                rotationAngle = 90F
            }

            MediaInfo.GRAVITY_TOP -> {
                layoutParams.gravity = Gravity.CENTER
                layoutParams.setMargins(0, 0, 0, 0)
                rotationAngle = 180F
            }

            MediaInfo.GRAVITY_END -> {
                layoutParams.gravity = Gravity.END
                layoutParams.setMargins(0, 0, 0, 0)
                rotationAngle = 270F
            }

            else -> {
                layoutParams.gravity = Gravity.CENTER
                layoutParams.setMargins(0, 450, 0, 0)
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
            decorView.addView(modifiedVV,0)
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
}