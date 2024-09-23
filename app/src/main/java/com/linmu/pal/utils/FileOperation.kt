package com.linmu.pal.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.linmu.pal.DataHolder
import com.linmu.pal.entity.MediaInfo
import java.io.File

class FileOperation {
    companion object {
        const val TAG: String = "FileOperation"
        const val ThumbnailWidth: Int = 300
        const val ThumbnailHeight: Int = 80
        const val CompressQuality: Int = 90
        const val RecordFileName: String = "DataList.json"

        // read and load the data in fileDir/record/RecordFileName to DataHolder
        fun readRecord(context: Context) {
            val destinationDir = File(context.filesDir, "record")
            val inputStream = File(destinationDir, RecordFileName).inputStream()
            val bufferReader = inputStream.bufferedReader()
            val gson = Gson()
            val type = object : TypeToken<MutableList<MediaInfo>>() {}.type
            val readResult = gson.fromJson<MutableList<MediaInfo>>(bufferReader, type)
            readResult?.forEach { mediaInfo ->
                if (DataHolder.mediaList.size == 0) {
                    DataHolder.mediaList.add(mediaInfo)
                } else if (!DataHolder.mediaList.contains(mediaInfo)) {
                    DataHolder.mediaList.add(mediaInfo)
                }
            }
            bufferReader.close()
            inputStream.close()
            Log.d(TAG, "ReadRecord: Done")
        }

        // write current list in DataHolder to fileDir/record/RecordFileName
        fun writeRecord(context: Context) {
            val gson = Gson()
            val json = gson.toJson(DataHolder.mediaList)
            val destinationDir = File(context.filesDir, "record")
            val outputStream = File(destinationDir, RecordFileName).outputStream()
            outputStream.write(json.toByteArray())
            outputStream.close()
            Log.d(TAG, "WriteRecord: Done DataSize:${DataHolder.mediaList.size}")
        }

        fun createSubDir(context: Context, subDirName: String): Boolean {
            // this function use to create subdir in fileDir
            val subFolder = File(context.filesDir, subDirName)
            return if (subFolder.exists()) {
                false
            } else {
                subFolder.mkdir()
            }
        }

        fun createImageThumbnail(context: Context, imageUri: Uri) {
            val filename = getImageNameFromUri(context, imageUri) ?: "Unknown"
            val destinationDir = File(context.filesDir, "thumbnail")
            val inputStreamForThumbnail = context.contentResolver.openInputStream(imageUri)
            val oriBitmap = BitmapFactory.decodeStream(inputStreamForThumbnail)
            val thumbnail =
                ThumbnailUtils.extractThumbnail(oriBitmap, ThumbnailWidth, ThumbnailHeight)
            val outputStream = File(destinationDir, filename).outputStream()
            thumbnail.compress(Bitmap.CompressFormat.JPEG, CompressQuality, outputStream)
            thumbnail.recycle()
            oriBitmap.recycle()
            inputStreamForThumbnail?.close()
            outputStream.close()
        }

        fun createVideoThumbnail(context: Context, videoUri: Uri) {
            val filename = getVideoNameFromUri(context, videoUri) ?: "Unknown"
            val resourceFile = File(context.filesDir, "media")
            val destinationDir = File(context.filesDir, "thumbnail")
            val videoFile = File(resourceFile, filename)
            val thumbnailSize = Size(ThumbnailWidth, ThumbnailHeight)
            val thumbnailBitmap =
                ThumbnailUtils.createVideoThumbnail(videoFile, thumbnailSize, null)
            val outputStream = File(destinationDir, filename).outputStream()
            thumbnailBitmap.compress(Bitmap.CompressFormat.JPEG, CompressQuality, outputStream)
            outputStream.close()
            thumbnailBitmap.recycle()
        }

        fun deleteFile(context: Context, filename: String): Boolean {
            val mediaDir = File(context.filesDir, "media")
            val thumbnailDir = File(context.filesDir, "thumbnail")
            val mediaToDelete = File(mediaDir, filename)
            val flag1 = mediaToDelete.delete()
            val thumbnailToDelete = File(thumbnailDir, filename)
            val flag2 = thumbnailToDelete.delete()
            if (flag1 && flag2) {
                DataHolder.deleteByMediaName(filename)
            }
            Log.d(TAG, "DeleteFile:$filename media-$flag1 thumbnail-$flag2")
            return (flag1 && flag2)
        }

        fun copyCacheFileToDir(context: Context, cacheFileUri: Uri): Boolean {
            // as a inner file, use lastPathSegment to get filename instead of query
            val filename = cacheFileUri.lastPathSegment
            filename?.let {
                val destinationDir = File(context.filesDir, "media")
                val outputStream = File(destinationDir, filename).outputStream()
                val inputStream = File(context.cacheDir, filename).inputStream()
                inputStream.copyTo(outputStream)
                inputStream.close()
                outputStream.close()
                return true
            }
            return false
        }

        fun copyVideoToDir(context: Context, videoUri: Uri): Boolean {
            val filename = getVideoNameFromUri(context, videoUri) ?: "Unknown"
            if (DataHolder.filenameExist(filename)) return false
            val inputStreamForCopy = context.contentResolver.openInputStream(videoUri)
            val destinationDir = File(context.filesDir, "media")
            val outputStream = File(destinationDir, filename).outputStream()
            inputStreamForCopy?.copyTo(outputStream)
            inputStreamForCopy?.close()
            outputStream.close()
            return EntityTransition.videoToEntity(context, videoUri, filename)
        }

        /**
         * fail may caused by duplication
         *
         * @return false for fail, true for done
         */
        fun copyImageToDir(context: Context, imageUri: Uri): Boolean {
            val filename = getImageNameFromUri(context, imageUri) ?: "Unknown"
            if (DataHolder.filenameExist(filename)) return false
            val inputStreamForCopy = context.contentResolver.openInputStream(imageUri)
            val destinationDir = File(context.filesDir, "media")
            val outputStream = File(destinationDir, filename).outputStream()
            inputStreamForCopy?.copyTo(outputStream)
            inputStreamForCopy?.close()
            outputStream.close()
            return EntityTransition.imageToEntity(context, imageUri, filename)
        }

        fun getImageNameFromUri(context: Context, imageUri: Uri): String? {
            val projection = arrayOf(MediaStore.Images.Media.DISPLAY_NAME)
            val cursor = context.contentResolver.query(imageUri, projection, null, null, null)
            return cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                    it.getString(nameIndex)
                } else {
                    null
                }
            }
        }

        fun getVideoNameFromUri(context: Context, videoUri: Uri): String? {
            val projection = arrayOf(MediaStore.Video.Media.DISPLAY_NAME)
            val cursor = context.contentResolver.query(videoUri, projection, null, null, null)
            return cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                    it.getString(nameIndex)
                } else {
                    null
                }
            }
        }
    }
}