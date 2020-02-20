package com.redspace.videoenginepoc

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.redspace.videoenginepoc.extensions.createMediaSource
import com.redspace.videoenginepoc.upstream.ByteStreamDataSource
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val player = SimpleExoPlayer.Builder(this).build()
        playerView.player = player

        val ioScope = CoroutineScope(Dispatchers.IO)

        val videoDelegate = ByteStreamDelegateImpl(assets.open("video-2019-12-20T15_44_00.668Z.mp4"))
        val videoDataSourceFactory = ByteStreamDataSource.Factory(videoDelegate, ioScope)
        val videoSource = ProgressiveMediaSource.Factory(videoDataSourceFactory).createMediaSource()

        val audioDelegate = ByteStreamDelegateImpl(assets.open("audio-2019-12-20T15_44_01.860Z.mp4"))
        val audioDataSourceFactory = ByteStreamDataSource.Factory(audioDelegate, ioScope)
        val audioSource = ProgressiveMediaSource.Factory(audioDataSourceFactory).createMediaSource()

        val mediaSource = MergingMediaSource(videoSource, audioSource)
        player.prepare(mediaSource)
    }
}
