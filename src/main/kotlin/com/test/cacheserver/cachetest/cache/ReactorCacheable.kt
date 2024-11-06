package com.test.cacheserver.cachetest.cache

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class ReactorCacheable(val name: String)
