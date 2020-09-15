package com.study.mytest

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.study.mytest.databinding.ActivityMainBinding
import com.study.mytest.viewmodel.VideoLoaderViewModel


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var loaderViewModel: VideoLoaderViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(binding.toolbar)
        initViewModels()
        binding.loadVideos.setOnClickListener {
            loaderViewModel.loadVideos(this@MainActivity)
        }
    }

    private fun initViewModels() {
        loaderViewModel = ViewModelProvider(this).get(VideoLoaderViewModel::class.java)
        initViewModelObservers()
    }

    private fun initViewModelObservers() {
        loaderViewModel.videoUriData.observe(this, { videoUri ->
            videoUri?.let {
                startActivity(VideoPlayerActivity.createIntent(this, it, true))
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode == VideoLoaderViewModel.SELECT_VIDEO_REQUEST && resultCode == RESULT_OK) {
            loaderViewModel.onLoadedVideoUri(intent?.data)
        }
        super.onActivityResult(requestCode, resultCode, intent)
    }
}