package com.example.elastic_playgroud

import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis


fun main() = runBlocking {
    measureTimeMillis {
        val deferreds: List<Deferred<Int>> = (1..3).map {
            async {
                calculate(it)
            }
        }
        val sum = deferreds.awaitAll().sum()
        println("$sum")
    }.let {
        println("time: $it")
    }
}

private suspend fun calculate(it: Int): Int {
    delay(1000L * it)
    println("Loading $it")
    return it
}

