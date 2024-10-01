package com.linmu.pal.mediahandler

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.linmu.pal.DataHolder
import com.linmu.pal.entity.MediaInfo
import com.linmu.pal.utils.DeviceMeta
import com.linmu.pal.utils.FileOperation
import com.linmu.pal.utils.MediaMeta
import com.yalantis.ucrop.UCrop
import java.io.File

class CropImageLauncher(
    private val activity:AppCompatActivity
) {
    private lateinit var newMediaInfo:MediaInfo
    private var mediaIndex:Int = 0
    private val cropLauncher =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
            if (result.resultCode == Activity.RESULT_OK){
                val data: Intent? = result.data
                data?.let {
                    val resultUri: Uri? = UCrop.getOutput(it)
                    resultUri?.let {
                        // copy and cover file from cacheDir to fileDir/media
                        if (FileOperation.copyCacheFileToDir(activity,resultUri)){
                            // update the mediainfo
                            val (newWidth,newHeight) = MediaMeta.getImageDimensions(activity,resultUri)
                            newMediaInfo.mediaWidth = newWidth
                            newMediaInfo.mediaHeight = newHeight
                            DataHolder.mediaList[mediaIndex] = newMediaInfo
                            Toast.makeText(activity, "Crop Finished", Toast.LENGTH_SHORT).show()
                        }else{
                            Toast.makeText(activity, "Crop Fail !", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

    /**
     * Crop the image source from DataHolder[position], result to cacheDir
     * After crop finished, if success, launcher will copy and cover the original file in fileDir/media
     *
     * @param position the position of process image
     */
    fun launch(position:Int){
        mediaIndex = position
        newMediaInfo = DataHolder.mediaList[position].deepCopy()
        val sourceDir = File(activity.filesDir,"media")
        val sourcePath = File(sourceDir, DataHolder.mediaList[position].mediaName).absolutePath
        val destinationPath = File(activity.cacheDir, DataHolder.mediaList[position].mediaName).absolutePath
        val uCrop = UCrop.of(Uri.fromFile(File(sourcePath)), Uri.fromFile(File(destinationPath)))
        val (width, height) = DeviceMeta.getMaxBounds(activity)
        val options = UCrop.Options().apply {
            setCompressionQuality(100)
        }
        uCrop.withOptions(options)
        if (newMediaInfo.orientation == MediaInfo.ORIENTATION_PORTRAIT){
            uCrop.withAspectRatio(width.toFloat(), height.toFloat())
        }else{
            uCrop.withAspectRatio(height.toFloat(), width.toFloat())
        }
        val uCropIntent = uCrop.getIntent(activity)
        cropLauncher.launch(uCropIntent)
    }
}