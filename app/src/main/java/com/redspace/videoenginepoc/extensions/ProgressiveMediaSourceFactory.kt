package com.redspace.videoenginepoc.extensions

import android.net.Uri
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource

fun ProgressiveMediaSource.Factory.createMediaSource(): MediaSource {
    return createMediaSource(Uri.EMPTY)
}