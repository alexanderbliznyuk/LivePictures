package com.blizniuk.livepictures

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@HiltAndroidApp
class LivePicturesApplication : Application()


fun main() {
    val f = flow<Int> {
        var i = 1
        try {
            while (true) {
                emit(i++)
                delay(1000)
            }
        } catch (e: CancellationException) {
            println("Cancellation exception")
            throw e
        }
    }

    runBlocking {
        val j = launch {
            f.collect {
                println("$it")
            }
        }

        delay(5000)
        j.cancel()
        delay(5000)
    }

}

