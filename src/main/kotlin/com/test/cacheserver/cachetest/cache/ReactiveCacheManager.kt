package com.test.cacheserver.cachetest.cache

import org.springframework.cache.CacheManager
import org.springframework.stereotype.Component
import reactor.cache.CacheFlux
import reactor.cache.CacheMono
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Signal
import java.util.function.Supplier

@Component
class ReactiveCacheManager(private val cacheManager: CacheManager) {

    fun <T> findCachedMono(
        cacheName: String,
        key: Any,
        retriever: Supplier<Mono<T>>,
        classType: Class<T>
    ): Mono<T> {
        val cache = cacheManager.getCache(cacheName)
        return CacheMono
            .lookup({ k ->
                val result = cache?.get(k, classType)
                result?.let { Mono.just(Signal.next(it)) } ?: Mono.empty()
            }, key)
            .onCacheMissResume(Mono.defer(retriever))
            .andWriteWith { k, signal ->
                Mono.fromRunnable {
                    if (!signal.isOnError) {
                        cache?.put(k, signal.get())
                    }
                }
            }
    }

    fun <T> findCachedFlux(
        cacheName: String,
        key: Any,
        retriever: Supplier<Flux<T>>
    ): Flux<T> {
        val cache = cacheManager.getCache(cacheName)
        return CacheFlux
            .lookup({ k ->
                @Suppress("UNCHECKED_CAST")
                val result = cache?.get(k, List::class.java) as? List<T>
                Mono.justOrEmpty(result)
                    .flatMap { list -> Flux.fromIterable(list).materialize().collectList() }
            }, key)
            .onCacheMissResume(Flux.defer(retriever))
            .andWriteWith { k, signalList ->
                Flux.fromIterable(signalList)
                    .dematerialize<T>()
                    .collectList()
                    .doOnNext { list -> cache?.put(k, list) }
                    .then()
            }
    }
}