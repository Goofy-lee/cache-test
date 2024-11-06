package com.test.cacheserver.cachetest

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CacheTestApplication

fun main(args: Array<String>) {
    runApplication<CacheTestApplication>(*args)
}
