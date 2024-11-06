package com.test.cacheserver.cachetest

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class SampleController(
    private val sampleService: SampleService
) {

    @GetMapping("/test")
     fun test(@RequestParam num: Int): Mono<String> {
        return sampleService.simulateNetworkCall(num)
    }
}