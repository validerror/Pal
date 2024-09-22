package com.linmu.pal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowInsetsController
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.linmu.pal.mediapicker.MultiImagePicker
import com.linmu.pal.mediapicker.SingleImagePicker
import com.linmu.pal.mediapicker.SingleVideoPicker
import com.linmu.pal.utils.FileOperation
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val TAG: String = "Activity_MA"

    private lateinit var singleImageIV: ImageView
    private lateinit var multiImageIV: ImageView
    private lateinit var singleVideoIV: ImageView
    private lateinit var thumbnailRV: RecyclerView
    private lateinit var thumbnailAdapter: ThumbnailAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // initialize
        val appInitializedSP = getSharedPreferences("Pal", MODE_PRIVATE)
        val initialized = appInitializedSP.getBoolean("initialized", false)
        if (!initialized) {
            appInitialize()
            val editor = appInitializedSP.edit()
            editor.putBoolean("initialized", true)
            editor.apply()
        }else{
            // initialized the DataHolder
            FileOperation.readRecord(this)
        }
        // layout out id bind
        bindView()
        // event
        bindEvent()
        // recyclerView
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        thumbnailRV.layoutManager = linearLayoutManager
        thumbnailAdapter = ThumbnailAdapter(this){deletePosition->
            // delete file
            if (FileOperation.deleteFile(this,DataHolder.mediaList[deletePosition].mediaName)){
                Log.d(TAG, "Delete Done Notify Change $deletePosition")
                // notify change
                thumbnailAdapter.notifyItemRemoved(deletePosition)
                FileOperation.writeRecord(this)
            }
        }
        thumbnailRV.adapter = thumbnailAdapter
    }

    private fun appInitialize() {
        // create necessary dir under fileDir
        FileOperation.createSubDir(this, "media")
        FileOperation.createSubDir(this, "thumbnail")
        FileOperation.createSubDir(this, "record")
        Log.d(TAG, "Created fileDir SubDir")
    }

    private fun bindView() {
        singleImageIV = findViewById(R.id.ma_iv_singleImage)
        multiImageIV = findViewById(R.id.ma_iv_multiImage)
        singleVideoIV = findViewById(R.id.ma_iv_singleVideo)
        thumbnailRV = findViewById(R.id.ma_rv_thumbnail)
    }

    private fun bindEvent() {
        // ActivityResultLauncher
        val singleImagePicker = SingleImagePicker(this)
        val multiImagePicker = MultiImagePicker(this)
        val singleVideoPicker = SingleVideoPicker(this)
        // click event
        singleImageIV.setOnClickListener {
            lifecycleScope.launch {
                val res = singleImagePicker.launch()
                if (res != 0){
                    thumbnailAdapter.notifyItemInserted(DataHolder.mediaList.size - 1)
                    FileOperation.writeRecord(this@MainActivity)
                }
            }
        }
        multiImageIV.setOnClickListener {
            val startPosition = DataHolder.mediaList.size
            lifecycleScope.launch {
                val res = multiImagePicker.launch()
                if (res != 0){
                    thumbnailAdapter.notifyItemRangeInserted(startPosition,res)
                    FileOperation.writeRecord(this@MainActivity)
                }
            }
        }
        singleVideoIV.setOnClickListener {
            lifecycleScope.launch {
                val res = singleVideoPicker.launch()
                if (res != 0){
                    thumbnailAdapter.notifyItemInserted(DataHolder.mediaList.size -1)
                    FileOperation.writeRecord(this@MainActivity)
                }
            }
        }
    }
}
