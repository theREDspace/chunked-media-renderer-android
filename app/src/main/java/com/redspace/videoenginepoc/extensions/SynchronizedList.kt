package com.redspace.videoenginepoc.extensions

import java.util.*

inline fun <T> synchronizedListOf() = Collections.synchronizedList(mutableListOf<T>())