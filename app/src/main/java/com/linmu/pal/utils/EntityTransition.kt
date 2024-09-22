package com.linmu.pal.utils

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import com.linmu.pal.DataHolder
import com.linmu.pal.entity.MediaInfo

class EntityTransition {
    companion object{
        fun videoToEntity(context: Context,videoUri: Uri,filename: String):Boolean{
            val (width,height) = VideoMeta.getVideoDimensions(context,videoUri)
            var orientation = MediaInfo.ORIENTATION_LANDSCAPE
            var gravity = MediaInfo.GRAVITY_BOTTOM
            if (width < height){
                orientation = MediaInfo.ORIENTATION_PORTRAIT
                gravity = MediaInfo.GRAVITY_BOTTOM
            }
            val mediaInfo = MediaInfo(
                MediaInfo.TYPE_VIDEO,orientation,
                gravity,
                width,height,filename
            )
            DataHolder.mediaList.add(mediaInfo)
            return true
        }

        // Image always keep portrait till crop by user
        fun imageToEntity(context: Context,imageUri: Uri,filename:String):Boolean{
            val inputStreamForInfo = context.contentResolver.openInputStream(imageUri)
            val bitmapOption = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            val container = BitmapFactory.decodeStream(inputStreamForInfo,null,bitmapOption)
            val mediaInfo = MediaInfo(
                MediaInfo.TYPE_IMAGE, MediaInfo.ORIENTATION_PORTRAIT,
                MediaInfo.GRAVITY_BOTTOM,
                bitmapOption.outWidth,bitmapOption.outHeight,filename)
            DataHolder.mediaList.add(mediaInfo)
            container?.recycle()
            inputStreamForInfo?.close()
            return true
        }
    }
}