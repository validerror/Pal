package com.linmu.pal.utils

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri

class VideoMeta {
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
    }
}