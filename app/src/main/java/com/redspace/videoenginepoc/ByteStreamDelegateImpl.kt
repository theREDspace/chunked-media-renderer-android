package com.redspace.videoenginepoc

import com.redspace.videoenginepoc.extensions.toByteArray
import com.redspace.videoenginepoc.upstream.ByteStreamDelegate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.InputStream

/**
 * Fake implementation that just converts a ByteArray to a Coroutine Flow
 */
class ByteStreamDelegateImpl(inputStream: InputStream) : ByteStreamDelegate {

    private val data: ByteArray = inputStream.toByteArray()

    override val contentLength: Int
        get() = data.size

    override fun bytes(): Flow<ByteArray> = flow {
        data.asList()
            .chunked(1024)
            .forEach { blob ->
                emit(blob.toByteArray())
            }
    }
}