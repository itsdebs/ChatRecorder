package com.vagabond.chatrecorder

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.media.MediaRecorder.AudioEncoder
import android.media.MediaRecorder.OutputFormat.THREE_GPP
import android.media.MediaRecorder.AudioSource
import android.media.MediaRecorder.AudioSource.MIC
import android.util.Log
import java.io.File
import java.io.IOException


//private class MediaRecorder(val context: Context){
    private var recorder:MediaRecorder?= null
    private var filePath:String?= null
    fun startRecording(context:Context):Boolean{
        val file = File(context.filesDir,  System.currentTimeMillis().toString() + ".m4a")
        recorder = MediaRecorder()
        recorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        recorder?.setAudioSamplingRate(44100);
        recorder?.setAudioEncodingBitRate(96000);
        recorder?.setOutputFile(file.absolutePath)
        recorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

        try {
            recorder?.prepare()
        } catch (e: IOException) {
            Log.e("recorder", "prepare() failed")
            return false
        }

        filePath = file.absolutePath
        recorder?.start()

        return true

    }
    @Throws(IllegalStateException::class)
    fun stopRecording():String?{
        recorder?.stop()
        recorder?.release()
        recorder = null
        return filePath
    }

    private var mPlayer:MediaPlayer?= null
    fun startPlaying(filePath:String):Boolean{
        mPlayer = MediaPlayer()
        try {
            mPlayer?.setDataSource(filePath)
            mPlayer?.prepare()
            mPlayer?.start()
            mPlayer?.setOnCompletionListener {
                it.stop()
                it.release()
                mPlayer = null
            }
        } catch (e: IOException) {
            Log.e("player", "prepare() failed")
            return false
        }
        return true
    }

    fun stopPlaying():Boolean{
        if(mPlayer == null) return false
        mPlayer?.stop()
        mPlayer?.release()
        mPlayer = null
        return true
    }
//}