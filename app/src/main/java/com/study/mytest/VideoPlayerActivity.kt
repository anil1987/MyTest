package com.study.mytest

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.study.mytest.databinding.ActivityVideoPlayerBinding
import com.study.mytest.viewmodel.VideoLoaderViewModel

class VideoPlayerActivity : AppCompatActivity() {

    companion object Factory {
        private const val KEY_VIDEO_URL = "video_url"
        private const val KEY_COMPRESS = "compress"
        private const val PERMISSION_WRITE_EXTERNAL_STORAGE = 1000

        @JvmStatic
        fun createIntent(
            context: Context, videoUri: Uri, compress: Boolean
        ): Intent {
            val intent = Intent(context, VideoPlayerActivity::class.java)
            intent.putExtra(KEY_VIDEO_URL, videoUri)
            intent.putExtra(KEY_COMPRESS, compress)
            return intent
        }
    }

    private lateinit var binding: ActivityVideoPlayerBinding
    private lateinit var loaderViewModel: VideoLoaderViewModel
    private var intetVideoUri: Uri? = null
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_player)
        initViewModels()
        readIntentData()
        initClickListeners()
    }

    private fun initClickListeners() {
        binding.compress.setOnClickListener {
            loaderViewModel.checkCompressSupported(this)
        }
        binding.playOrPause.setOnClickListener {
            if (binding.videoView.isPlaying) {
                pausePlayer()
            } else {
                resumePlayer()
            }
        }
    }

    private fun pausePlayer() {
        binding.playOrPause.text = getString(R.string.str_play)
        mediaPlayer?.pause()
    }

    private fun resumePlayer() {
        mediaPlayer?.start()
        binding.playOrPause.text = getString(R.string.str_pause)
    }


    private fun initViewModels() {
        loaderViewModel = ViewModelProvider(this).get(VideoLoaderViewModel::class.java)
        initViewModelObservers()
    }

    private fun initViewModelObservers() {
        loaderViewModel.compressSupportData.observe(this, { isSupported ->
            if (isSupported) {
                mediaPlayer?.pause()
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    // request the permission
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        PERMISSION_WRITE_EXTERNAL_STORAGE
                    )
                } else {
                    loaderViewModel.compressVideo(this@VideoPlayerActivity, intetVideoUri)
                }

            } else {
                onCompressFailure(getString(R.string.str_compress_not_supported))
            }
        })

        loaderViewModel.progressData.observe(this, { show ->
            binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        })
        loaderViewModel.videoUriData.observe(this, { videoUri ->
            videoUri?.let {
                startActivity(createIntent(this, it, false))
                finish()
            } ?: run {
                onCompressFailure(getString(R.string.str_compress_not_supported))
            }
        })
    }

    private fun onCompressFailure(message: String) {
        showAlertDialog(
            message
        ) { dialog, _ ->
            dialog?.dismiss()
            showOrHideCompressOptions(false)
            resumePlayer()
        }
    }

    private fun readIntentData() {
        intetVideoUri = intent.getParcelableExtra<Uri>(KEY_VIDEO_URL)
        var compress = intent.getBooleanExtra(KEY_COMPRESS, false)
        intetVideoUri?.let {
            playVideo(it)
        } ?: run {
            compress = false;
        }
        showOrHideCompressOptions(compress)
    }

    private fun showOrHideCompressOptions(compress: Boolean) {
        binding.compressLayout.visibility = if (compress) View.VISIBLE else View.GONE
        binding.videoOptionsLayout.visibility = if (compress) View.GONE else View.VISIBLE
    }

    private fun playVideo(videoUri: Uri?) {
        videoUri?.let {
            binding.apply {
                videoView.setVideoPath(videoUri.toString())
                videoView.requestFocus()
                videoView.setOnPreparedListener { mp -> mediaPlayer = mp }
                videoView.setOnCompletionListener { mp ->
                    mediaPlayer = mp
                    binding.playOrPause.text = getString(R.string.str_play)
                }
                videoView.start()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_WRITE_EXTERNAL_STORAGE -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    // permission was granted.
                    loaderViewModel.compressVideo(this@VideoPlayerActivity, intetVideoUri)
                } else {
                    // permission denied.
                    // tell the user the action is cancelled
                    onCompressFailure(getString(R.string.video_cannot_compress))
                }
                return
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun showAlertDialog(message: String, onClickListener: DialogInterface.OnClickListener) {
        val alertDialog: AlertDialog = AlertDialog.Builder(this).create()
        alertDialog.setMessage(message)
        alertDialog.setButton(
            AlertDialog.BUTTON_NEUTRAL,
            getString(R.string.str_ok),
            onClickListener
        )
        alertDialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer = null
    }

}