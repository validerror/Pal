package com.linmu.pal.utils

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import com.linmu.pal.DataHolder
import com.linmu.pal.entity.MediaInfo
import java.io.File

class EntityTransition {
    companion object {
        fun videoToEntity(context: Context, videoUri: Uri, filename: String): Boolean {
            val (width, height) = MediaMeta.getVideoDimensions(context, videoUri)
            var orientation = MediaInfo.ORIENTATION_LANDSCAPE
            var gravity = MediaInfo.GRAVITY_BOTTOM
            if (width < height) {
                orientation = MediaInfo.ORIENTATION_PORTRAIT
                gravity = MediaInfo.GRAVITY_BOTTOM
            }
            val mediaInfo = MediaInfo(
                MediaInfo.TYPE_VIDEO, orientation,
                gravity,
                width, height, filename
            )
            DataHolder.mediaList.add(mediaInfo)
            return true
        }

        fun imageToEntity(context: Context, imageUri: Uri, filename: String): Boolean {
            val (width, height) = MediaMeta.getImageDimensions(context, imageUri)
            var orientation = MediaInfo.ORIENTATION_PORTRAIT
            if (width > height){
                orientation = MediaInfo.ORIENTATION_LANDSCAPE
            }
            val mediaInfo = MediaInfo(
                MediaInfo.TYPE_IMAGE, orientation,
                MediaInfo.GRAVITY_BOTTOM,
                width, height, filename
            )
            DataHolder.mediaList.add(mediaInfo)
            return true
        }

        fun addThumbnailToDataHolder(context: Context, filename: String) {
            val destinationDir = File(context.filesDir, "thumbnail")
            val thumbnailFile = File(destinationDir, filename)
            val thumbnailBitmap = BitmapFactory.decodeFile(thumbnailFile.absolutePath)
            DataHolder.thumbnailList.add(thumbnailBitmap)
        }
    }
}