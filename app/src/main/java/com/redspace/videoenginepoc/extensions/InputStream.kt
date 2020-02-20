package com.redspace.videoenginepoc.extensions

import java.io.InputStream

// converts InputStream to ByteArray and closes it.
fun InputStream.toByteArray(): ByteArray {
    return try {
        readBytes()
    } catch (error: Throwable) {
        throw error
    } finally {
        close()
    }
}