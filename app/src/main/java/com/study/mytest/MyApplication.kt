package com.study.mytest

import android.app.Application

class MyApplication : Application() {

    companion object Factory {
        private lateinit var instance: MyApplication

        @JvmStatic
        fun instance(): Application {
            return instance
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}