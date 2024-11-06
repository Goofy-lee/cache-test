package com.test.cacheserver.cachetest.cache

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
@EnableCaching
class CacheConfig {

    @Bean
    fun caffeineConfig(): Caffeine<Any, Any> {
        return Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)  // TTL 설정
            .maximumSize(100)  // 최대 저장 단위
    }

    @Bean
    fun cacheManager(): CaffeineCacheManager {
        val cacheManager = CaffeineCacheManager()
        cacheManager.setCaffeine(caffeineConfig())
        return cacheManager
    }
}