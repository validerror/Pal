package com.linmu.pal.mediapicker

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.linmu.pal.utils.FileOperation
import kotlinx.coroutines.CompletableDeferred

class SingleVideoPicker(
    private val activity:AppCompatActivity
) {
    private lateinit var deferred: CompletableDeferred<Int>
    private var itemChange:Int = 0
    private val pickerLauncher =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
            if (result.resultCode == Activity.RESULT_OK){
                val uri: Uri? = result.data?.data
                val path:String? = result.data?.data?.path
                uri?.let {
                    FileOperation.copyVideoToDir(activity,uri)
                    FileOperation.createVideoThumbnail(activity,uri)
                    itemChange = 1
                }
                deferred.complete(itemChange)
            }
        }

    /**
     * @return 0 for fail, 1 for success
     */
    suspend fun launch():Int{
        deferred = CompletableDeferred()
        itemChange = 0
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "video/*"
            }
        pickerLauncher.launch(intent)
        return deferred.await()
    }
}