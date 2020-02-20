package com.redspace.videoenginepoc.upstream

import android.net.Uri
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.upstream.BaseDataSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.redspace.videoenginepoc.extensions.synchronizedListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.math.min

/**
 * This ByteStreamDataSource is a copy of ByteArrayDataSource from ExoPlayer.
 * The only difference is that ByteStreamDataSource receives a stream of bytes and appends it to the initial data and perform.
 * It class performs a simplified data synchronisation just for the POC purpose.
 */
class ByteStreamDataSource(
    private val delegate: ByteStreamDelegate,
    scope: CoroutineScope
) : BaseDataSource(false) {

    private var uri: Uri? = null
    private val data = synchronizedListOf<Byte>()
    private var readPosition = 0
    private var bytesRemaining = 0
    private var opened = false
    private val contentLength: Int by lazy { delegate.contentLength }

    init {
        scope.launch {
            delegate.bytes()
                .collect { blob ->
                    synchronized(data) {
                        blob.forEach { byte ->
                            data += byte
                        }
                    }
                }
        }
    }

    override fun open(dataSpec: DataSpec): Long {
        uri = dataSpec.uri
        transferInitializing(dataSpec)
        readPosition = dataSpec.position.toInt()
        bytesRemaining = if (dataSpec.size == C.LENGTH_UNSET) {
            (contentLength - dataSpec.position).toInt()
        } else {
            dataSpec.size
        }
        opened = true
        transferStarted(dataSpec)
        return bytesRemaining.toLong()
    }

    override fun getUri(): Uri? {
        return uri
    }

    override fun close() {
        if (opened) {
            opened = false
            transferEnded()
        }
        uri = null
    }

    override fun read(buffer: ByteArray, offset: Int, desiredReadLength: Int): Int {
        return when {
            desiredReadLength == 0 -> {
                0
            }
            bytesRemaining == 0 -> {
                C.RESULT_END_OF_INPUT
            }
            else -> {
                val readLength = min(desiredReadLength, bytesRemaining)
                var dataSize = synchronized(data) {
                    data.size
                }
                while (dataSize < readPosition + readLength) { // if
                    Thread.sleep(100)
                    dataSize = synchronized(data) {
                        data.size
                    }
                }

                val dataChunk = synchronized(data) {
                    data.subList(readPosition, readPosition + readLength).toByteArray()
                }

                System.arraycopy(dataChunk, 0, buffer, offset, readLength)
                readPosition += readLength
                bytesRemaining -= readLength
                bytesTransferred(readLength)
                readLength
            }
        }
    }

    class Factory(
        private val delegate: ByteStreamDelegate,
        private val scope: CoroutineScope
    ) : DataSource.Factory {

        override fun createDataSource(): DataSource = ByteStreamDataSource(delegate, scope)
    }
}

/**
 * Reads the data and provides stream of ByteArrays to ByteStreamDataSource
 */
interface ByteStreamDelegate {
    val contentLength: Int

    fun bytes(): Flow<ByteArray>
}

private val DataSpec.size: Int
    get() = length.toInt()