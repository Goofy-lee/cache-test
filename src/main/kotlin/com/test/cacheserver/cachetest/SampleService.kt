package com.test.cacheserver.cachetest

import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.temporal.ChronoUnit

@Service
class SampleService {
    fun simulateNetworkCall(num: Int = 0): Mono<String> {
        return Mono.just("NetworkIo $num counts")
            .doOnNext { println("$it 이 찍혔음") }
            .delayElement(Duration.of(3, ChronoUnit.SECONDS))
            .cache(Duration.of(10, ChronoUnit.SECONDS))
    }
}