package com.example.reporterapp

import android.content.Context
import android.view.SurfaceHolder
import android.media.MediaRecorder
import android.media.CamcorderProfile

class Utility{
    companion object {
        @Volatile
        private var INSTANCE: Utility? = null

        fun getUtility(context: Context): Utility {

            return INSTANCE ?: synchronized(this) {
                val instance = Utility()
                INSTANCE = instance
                instance
            }
        }
    }

    var recorder: MediaRecorder
    var holder: SurfaceHolder? = null
    var recording = false

    init {
        recorder = MediaRecorder()
        initRecorder()
    }

    private fun initRecorder() {
        recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT)
        recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT)

        val cpHigh = CamcorderProfile
            .get(CamcorderProfile.QUALITY_HIGH)
        recorder.setProfile(cpHigh)
        recorder.setOutputFile("/sdcard/videocapture_example.mp4")
        recorder.setMaxDuration(50000) // 50 seconds
        recorder.setMaxFileSize(5000000) // Approximately 5 megabytes
    }

}

