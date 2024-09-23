package com.linmu.pal.mediahandler

import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.linmu.pal.utils.FileOperation
import kotlinx.coroutines.CompletableDeferred

class MultiImagePicker(
    private val activity: AppCompatActivity
) {
    private lateinit var deferred: CompletableDeferred<Int>
    private var itemChange:Int = 0
    private val pickerLauncher =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val resultIntent = result.data
                resultIntent?.let {
                    if (it.action == Intent.ACTION_SEND_MULTIPLE) {
                        val clipData: ClipData? = it.clipData
                        if (clipData != null) {
                            for (i in 0 until clipData.itemCount) {
                                val item: ClipData.Item = clipData.getItemAt(i)
                                val uri: Uri? = item.uri
                                uri?.let {
                                    val flag1 = FileOperation.copyImageToDir(activity, uri)
                                    if (flag1){
                                        FileOperation.createImageThumbnail(activity,uri)
                                        itemChange += 1
                                    }
                                }
                            }
                            deferred.complete(itemChange)
                        }
                    }
                }
            }
        }

    /**
     * Notice that Amount may less than the item inserted because of duplication
     *
     * @return 0 for fail, Amount for insertItems count
     */
    suspend fun launch():Int{
        deferred = CompletableDeferred()
        itemChange = 0
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        pickerLauncher.launch(intent)
        return deferred.await()
    }
}