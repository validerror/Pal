package com.linmu.pal

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.linmu.pal.mediahandler.CropImageLauncher
import com.linmu.pal.mediahandler.MultiImagePicker
import com.linmu.pal.mediahandler.SingleImagePicker
import com.linmu.pal.mediahandler.SingleVideoPicker
import com.linmu.pal.ui.MediaSettingDialogFragment
import com.linmu.pal.utils.FileOperation
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private val TAG: String = "Activity_MA"

    private lateinit var singleImageIV: ImageView
    private lateinit var multiImageIV: ImageView
    private lateinit var singleVideoIV: ImageView
    private lateinit var thumbnailRV: RecyclerView
    private lateinit var thumbnailAdapter: ThumbnailAdapter
    private lateinit var cropImageLauncher: CropImageLauncher
    private var settingDialog: MediaSettingDialogFragment? = null


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
        } else {
            // initialized the DataHolder
            FileOperation.readRecord(this)
        }
        // layout out id bind
        bindView()
        // event
        bindEvent()
        cropImageLauncher = CropImageLauncher(this)
        // recyclerView
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        thumbnailRV.layoutManager = linearLayoutManager
        thumbnailAdapter = ThumbnailAdapter(this,
            { deletePosition ->
                // delete file
                if (FileOperation.deleteFile(
                        this,
                        DataHolder.mediaList[deletePosition].mediaName
                    )
                ) {
                    Log.d(TAG, "Delete Done Notify Change $deletePosition")
                    // notify change
                    thumbnailAdapter.notifyItemRemoved(deletePosition)
                }
            },
            { settingPosition ->
                startSettingDialog(settingPosition)
                settingDialog?.show(supportFragmentManager, "settingDialog")
            })
        thumbnailRV.adapter = thumbnailAdapter
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onRestart() {
        super.onRestart()
        FileOperation.readRecord(this)
        thumbnailAdapter.notifyDataSetChanged()
    }

    override fun onStop() {
        super.onStop()
        FileOperation.writeRecord(this)
        DataHolder.releaseBitmap()
    }

    private fun startSettingDialog(settingPosition:Int){
        if (settingPosition != -1) {
            settingDialog = MediaSettingDialogFragment(settingPosition, cropImageLauncher)
        }
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
                if (res != 0) {
                    thumbnailAdapter.notifyItemInserted(DataHolder.mediaList.size - 1)
                    FileOperation.writeRecord(this@MainActivity)
                }
            }
        }
        multiImageIV.setOnClickListener {
            val startPosition = DataHolder.mediaList.size
            lifecycleScope.launch {
                val res = multiImagePicker.launch()
                if (res != 0) {
                    thumbnailAdapter.notifyItemRangeInserted(startPosition, res)
                    FileOperation.writeRecord(this@MainActivity)
                }
            }
        }
        singleVideoIV.setOnClickListener {
            lifecycleScope.launch {
                val res = singleVideoPicker.launch()
                if (res != 0) {
                    thumbnailAdapter.notifyItemInserted(DataHolder.mediaList.size - 1)
                    FileOperation.writeRecord(this@MainActivity)
                }
            }
        }
    }
}
