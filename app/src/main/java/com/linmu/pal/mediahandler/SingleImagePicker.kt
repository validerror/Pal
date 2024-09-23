package com.linmu.pal.mediahandler

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.linmu.pal.utils.FileOperation
import kotlinx.coroutines.CompletableDeferred

class SingleImagePicker(
    private val activity: AppCompatActivity
) {
    private lateinit var deferred: CompletableDeferred<Int>
    private var itemChanged:Int = 0
    private val pickerLauncher =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri: Uri? = result.data?.data
                uri?.let {
                    val flag1 = FileOperation.copyImageToDir(activity,uri)
                    if (flag1){
                        FileOperation.createImageThumbnail(activity,uri)
                        itemChanged = 1
                    }
                }
            }
            deferred.complete(itemChanged)
        }

    /**
     * @return 0 for fail, 1 for success
     */
    suspend fun launch():Int{
        deferred = CompletableDeferred()
        itemChanged = 0
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickerLauncher.launch(intent)
        return deferred.await()
    }
}