package com.linmu.pal.utils

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.linmu.pal.DataHolder
import com.linmu.pal.entity.MediaInfo

class MediaMeta {
    companion object {
        fun getVideoDimensions(context: Context, videoUri: Uri): Pair<Int, Int> {
            val retriever = MediaMetadataRetriever()
            return try {
                retriever.setDataSource(context, videoUri)
                val width =
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                        ?.toIntOrNull()
                val height =
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                        ?.toIntOrNull()
                if (width != null && height != null) Pair(width, height) else Pair(0, 0)
            } finally {
                retriever.release()
            }
        }

        fun getImageDimensions(context: Context,imageUri: Uri):Pair<Int,Int>{
            val inputStreamForInfo = context.contentResolver.openInputStream(imageUri)
            val bitmapOption = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            val container = BitmapFactory.decodeStream(inputStreamForInfo,null,bitmapOption)
            val width = bitmapOption.outWidth
            val height = bitmapOption.outHeight
            container?.recycle()
            inputStreamForInfo?.close()
            return width to height
        }
    }
}