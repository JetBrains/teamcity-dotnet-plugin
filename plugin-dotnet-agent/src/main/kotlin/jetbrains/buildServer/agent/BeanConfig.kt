

package jetbrains.buildServer.agent

import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.cache.support.SimpleCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
@EnableCaching
open class BeanConfig {
    @Bean
    open fun cacheManager(): CacheManager {
        return ConcurrentMapCacheManager()
    }
}