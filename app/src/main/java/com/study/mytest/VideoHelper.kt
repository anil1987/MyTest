package com.study.mytest

import android.content.Context
import android.util.Log
import com.study.mytest.utils.FileManager
import nl.bravobit.ffmpeg.ExecuteBinaryResponseHandler
import nl.bravobit.ffmpeg.FFmpeg

class VideoHelper(private val context: Context) {

    private var status = NONE
    private var errorMessage = "Compression Failed!"

    fun startCompressing(inputPath: String?, listener: VideoHelperListener?) {
        if (inputPath == null || inputPath.isEmpty()) {
            status = NONE
            listener?.onFinished(NONE, false, null)
            return
        }
        val pathName = inputPath.substring(inputPath.lastIndexOf("/"))
        val outputPath = FileManager.createFile(pathName).absolutePath
        val commandParams = arrayOfNulls<String>(26)
        commandParams[0] = "-y"
        commandParams[1] = "-i"
        commandParams[2] = inputPath
        commandParams[3] = "-s"
        commandParams[4] = "240x320"
        commandParams[5] = "-r"
        commandParams[6] = "20"
        commandParams[7] = "-c:v"
        commandParams[8] = "libx264"
        commandParams[9] = "-preset"
        commandParams[10] = "ultrafast"
        commandParams[11] = "-c:a"
        commandParams[12] = "copy"
        commandParams[13] = "-me_method"
        commandParams[14] = "zero"
        commandParams[15] = "-tune"
        commandParams[16] = "fastdecode"
        commandParams[17] = "-tune"
        commandParams[18] = "zerolatency"
        commandParams[19] = "-strict"
        commandParams[20] = "-2"
        commandParams[21] = "-b:v"
        commandParams[22] = "1000k"
        commandParams[23] = "-pix_fmt"
        commandParams[24] = "yuv420p"
        commandParams[25] = outputPath
        compressVideo(commandParams, outputPath, listener)
    }

    private fun compressVideo(
        command: Array<String?>,
        outputFilePath: String,
        listener: VideoHelperListener?
    ) {
        try {
            FFmpeg.getInstance(context).execute(command, object : ExecuteBinaryResponseHandler() {
                override fun onSuccess(message: String?) {
                    status = SUCCESS
                    Log.e("VideoCronProgress", "SUCCESS")
                }

                override fun onProgress(message: String?) {
                    status = RUNNING
                    Log.e("VideoCronProgress", message)
                }

                override fun onFailure(message: String) {
                    status = FAILED
                    Log.e("VideoCompressor", message)
                    listener?.onFailure("Error : $message")
                }

                override fun onStart() {
                    Log.e("VideoCompressor", "Start")
                }

                override fun onFinish() {
                    Log.e("VideoCronProgress", "finished at $status")
                    listener?.onFinished(status, true, outputFilePath)
                }
            })
        } catch (e: Exception) {
            status = FAILED
            errorMessage = e.message as String
            listener?.onFailure("Error : $errorMessage")
        }
    }

    interface VideoHelperListener {
        fun onFinished(status: Int, isVideo: Boolean, fileOutputPath: String?) {}
        fun onFailure(message: String?) {}
    }

    companion object {
        const val SUCCESS = 1
        const val FAILED = 2
        const val NONE = 3
        const val RUNNING = 4
    }
}