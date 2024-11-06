package com.test.cacheserver.cachetest.cache

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.core.ResolvableType
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

@Aspect
@Component
class ReactorCacheAspect(
    private val reactiveCacheManager: ReactiveCacheManager
) {

    @Pointcut("@annotation(ReactorCacheable)")
    fun pointcut() {
    }

    @Around("pointcut()")
    fun around(joinPoint: ProceedingJoinPoint): Any {
        val signature = joinPoint.signature as MethodSignature
        val method: Method = signature.method

        val parameterizedType = method.genericReturnType as ParameterizedType
        val rawType: Type = parameterizedType.rawType

        if (rawType != Mono::class.java && rawType != Flux::class.java) {
            throw IllegalArgumentException(
                "The return type is not Mono/Flux. Use Mono/Flux for return type. method: ${method.name}"
            )
        }

        val reactorCacheable = method.getAnnotation(ReactorCacheable::class.java)
        val cacheName = reactorCacheable.name
        val args = joinPoint.args

        return if (rawType == Mono::class.java) {
            val returnTypeInsideMono = parameterizedType.actualTypeArguments[0]
            val returnClass = ResolvableType.forType(returnTypeInsideMono).resolve() as Class<Any>

            val retriever: () -> Mono<Any> = {
                @Suppress("UNCHECKED_CAST")
                joinPoint.proceed(args) as Mono<Any>
            }

            reactiveCacheManager.findCachedMono(cacheName, generateKey(*args), retriever, returnClass)
                .doOnError { e ->
                    println("Failed to processing mono cache. method: ${method.name}, error: $e")
                }
        } else {
            val retriever: () -> Flux<Any> = {
                @Suppress("UNCHECKED_CAST")
                joinPoint.proceed(args) as Flux<Any>
            }

            reactiveCacheManager.findCachedFlux(cacheName, generateKey(*args), retriever)
                .doOnError { e ->
                    println("Failed to processing flux cache. method: ${method.name}, error: $e")
                }
        }
    }

    private fun generateKey(vararg objects: Any?): String {
        return objects.joinToString(":") { it?.toString() ?: "" }
    }
}