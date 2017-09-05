package jetbrains.buildServer.dotnet

import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.cache.ehcache.EhCacheCacheManager
import org.springframework.cache.guava.GuavaCacheManager
import org.springframework.cache.jcache.JCacheCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration
import java.util.concurrent.TimeUnit

@Configuration
@EnableCaching
open class CachingConfig {
    @Bean
    public open fun cacheManager(): CacheManager {
        return CustomCacheManager { TimeEvictStrategy(Duration.ofMinutes(1)) }
    }
}