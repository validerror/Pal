package com.linmu.pal.entity

data class MediaInfo(
    val type: Int,
    var orientation: Int,
    var gravity: Int,
    var mediaWidth: Int,
    var mediaHeight: Int,
    val mediaName: String,
    var enableClock: Boolean = true,
    var clockColor: Int = 0
) {
    fun deepCopy(): MediaInfo {
        return MediaInfo(
            type,
            orientation,
            gravity,
            mediaWidth,
            mediaHeight,
            mediaName,
            enableClock,
            clockColor
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MediaInfo) return false
        return mediaName == other.mediaName
    }

    override fun hashCode(): Int {
        return mediaName.hashCode()
    }

    fun rotateQuarter() {
        gravity = (gravity + 1) % 4
    }

    companion object {
        val TYPE_UNSET: Int = 0
        val TYPE_IMAGE: Int = 1
        val TYPE_VIDEO: Int = 2

        val ORIENTATION_PORTRAIT = 0
        val ORIENTATION_LANDSCAPE = 1

        val GRAVITY_START = 0
        val GRAVITY_TOP = 1
        val GRAVITY_END = 2
        val GRAVITY_BOTTOM = 3
    }
}