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
        private const val TAG: String = "FileOperation"
        private const val ThumbnailWidth: Int = 300
        private const val ThumbnailHeight: Int = 80
        private const val CompressQuality: Int = 90
        private const val RecordFileName: String = "DataList.json"
        private const val ThumbnailDirName: String = "thumbnail"
        private const val MediaDirName: String = "media"
        private const val RecordDirName: String = "record"
        private val gson = Gson()

        fun loadThumbnails(context: Context) {
            DataHolder.releaseBitmap()
            val destinationDir = File(context.filesDir, ThumbnailDirName)
            for (i in 0 until DataHolder.mediaList.size) {
                val thumbnailName = DataHolder.mediaList[i].mediaName
                val thumbnailFile = File(destinationDir, thumbnailName)
                val thumbnailBitmap = BitmapFactory.decodeFile(thumbnailFile.absolutePath)
                DataHolder.thumbnailList.add(thumbnailBitmap)
            }
        }

        // read and load the data in fileDir/record/RecordFileName to DataHolder
        fun readRecord(context: Context) {
            val destinationDir = File(context.filesDir, RecordDirName)
            val destinationFile = File(destinationDir, RecordFileName)
            val inputStream = destinationFile.inputStream()
            val bufferReader = inputStream.bufferedReader()
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
            loadThumbnails(context)
            Log.d(TAG, "ReadRecord: Done")
        }

        // write current list in DataHolder to fileDir/record/RecordFileName
        fun writeRecord(context: Context) {
            val json = gson.toJson(DataHolder.mediaList)
            val destinationDir = File(context.filesDir, RecordDirName)
            val outputStreamToFile = File(destinationDir, RecordFileName).outputStream()
            outputStreamToFile.use { outputStream ->
                val data = json.toByteArray()
                outputStream.write(data)
            }
            Log.d(TAG, "WriteRecord: Done")
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

        fun createImageThumbnail(context: Context, imageUri: Uri): String {
            val filename = getImageNameFromUri(context, imageUri) ?: "Unknown"
            val destinationDir = File(context.filesDir, ThumbnailDirName)
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
            return filename
        }

        fun createVideoThumbnail(context: Context, videoUri: Uri): String {
            val filename = getVideoNameFromUri(context, videoUri) ?: "Unknown"
            val resourceFile = File(context.filesDir, MediaDirName)
            val destinationDir = File(context.filesDir, ThumbnailDirName)
            val videoFile = File(resourceFile, filename)
            val thumbnailSize = Size(ThumbnailWidth, ThumbnailHeight)
            val thumbnailBitmap =
                ThumbnailUtils.createVideoThumbnail(videoFile, thumbnailSize, null)
            val outputStream = File(destinationDir, filename).outputStream()
            thumbnailBitmap.compress(Bitmap.CompressFormat.JPEG, CompressQuality, outputStream)
            outputStream.close()
            thumbnailBitmap.recycle()
            return filename
        }

        fun deleteFile(context: Context, filename: String): Boolean {
            val mediaDir = File(context.filesDir, MediaDirName)
            val thumbnailDir = File(context.filesDir, ThumbnailDirName)
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
                val destinationDir = File(context.filesDir, MediaDirName)
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
            val destinationDir = File(context.filesDir, MediaDirName)
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
            val destinationDir = File(context.filesDir, MediaDirName)
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