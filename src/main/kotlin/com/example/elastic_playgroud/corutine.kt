package com.example.elastic_playgroud

import kotlinx.coroutines.*
import kotlin.random.Random
import kotlin.system.measureTimeMillis


fun main() = runBlocking {
    measureTimeMillis {
        val deferreds: List<Deferred<Int>> = (1..3).map {
            async {
                delay(1000L * it)
                println("Loading $it")
                it
            }
        }
        val sum = deferreds.awaitAll().sum()
        println("$sum")
    }.let {
        println("time: $it")
    }
}

