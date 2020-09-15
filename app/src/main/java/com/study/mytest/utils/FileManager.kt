package com.study.mytest.utils

import android.content.Context
import android.content.ContextWrapper
import com.study.mytest.MyApplication
import java.io.File


object FileManager {

    private const val VIDEOS_DIRECTORY = "CompressedVideos"


    private val fileDirectory: File
        get() {
            val cw = ContextWrapper(MyApplication.instance())
            val folder: File = cw.getDir(
                VIDEOS_DIRECTORY, Context.MODE_PRIVATE
            )
            if (!folder.exists()) folder.mkdirs()
            return folder
        }

    fun createFile(name: String): File {
        val file = File(fileDirectory, name)
        file.createNewFile()
        return file
    }

}