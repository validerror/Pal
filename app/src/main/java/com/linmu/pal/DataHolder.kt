package com.linmu.pal

import com.linmu.pal.entity.MediaInfo

object DataHolder {
    var mediaList:MutableList<MediaInfo> = arrayListOf()

    fun deleteByMediaName(name:String){
        for (i in 0 until  mediaList.size){
            if (name == mediaList[i].mediaName){
                mediaList.removeAt(i)
                break
            }
        }
    }

    fun filenameExist(name: String):Boolean{
        if (mediaList.size == 0) return false
        for (i in 0 until  mediaList.size){
            if (name == mediaList[i].mediaName){
                return true
            }
        }
        return false
    }
}