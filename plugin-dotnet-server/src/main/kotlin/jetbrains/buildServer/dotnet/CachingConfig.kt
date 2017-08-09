package jetbrains.buildServer.dotnet

import com.google.common.cache.CacheBuilder
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.guava.GuavaCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
@EnableCaching
open class CachingConfig {
    @Bean
    public open fun cacheManager(): CacheManager {
        val cacheManager = GuavaCacheManager()
        cacheManager.setCacheBuilder(
                CacheBuilder.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(60, TimeUnit.SECONDS))
        return cacheManager
    }
}