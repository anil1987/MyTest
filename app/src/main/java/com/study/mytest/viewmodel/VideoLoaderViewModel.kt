package com.study.mytest.viewmodel

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.study.mytest.VideoHelper
import com.study.mytest.VideoHelper.VideoHelperListener
import nl.bravobit.ffmpeg.FFmpeg
import java.io.File


class VideoLoaderViewModel : ViewModel() {
    companion object {
        internal const val SELECT_VIDEO_REQUEST = 1
    }

    internal var videoUriData = MutableLiveData<Uri?>()
    internal var compressSupportData = MutableLiveData<Boolean>()
    internal var progressData = MutableLiveData<Boolean>()

    fun loadVideos(activity: Activity) {
        val galleryIntent = Intent(
            Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        )
        activity.startActivityForResult(galleryIntent, SELECT_VIDEO_REQUEST)
    }

    fun onLoadedVideoUri(videoUri: Uri?) {
        videoUriData.value = videoUri
    }

    fun checkCompressSupported(context: Context) {
        compressSupportData.value = FFmpeg.getInstance(context).isSupported
    }

    fun compressVideo(activity: Activity, uri: Uri?) {
        uri?.let {
            val videoPath = getVideoPath(activity, it)
            videoPath?.let {
                progressData.value = true
                val compressor = VideoHelper(activity)
                compressor.startCompressing(videoPath, object : VideoHelperListener {
                    override fun onFinished(
                        status: Int,
                        isVideo: Boolean,
                        fileOutputPath: String?
                    ) {
                        progressData.value = false
                        if (status == VideoHelper.SUCCESS) {
                            fileOutputPath?.let {
                                val outputFile = File(fileOutputPath)
                                onLoadedVideoUri(Uri.parse(outputFile.absolutePath))
                            }
                        }
                    }

                    override fun onFailure(message: String?) {
                        progressData.value = false
                        videoUriData.value = null
                    }
                })
            }
        }
    }

    @SuppressLint("Recycle")
    private fun getVideoPath(activity: Activity, uri: Uri): String? {
        val projection = arrayOf(MediaStore.Video.Media.DATA)
        val cursor = activity.contentResolver.query(uri, projection, null, null, null)
        return if (cursor != null) {
            val columnIndex: Int = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(columnIndex)
        } else null
    }
}