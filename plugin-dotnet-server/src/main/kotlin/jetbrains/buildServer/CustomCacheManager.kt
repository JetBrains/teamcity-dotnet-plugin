

package jetbrains.buildServer

import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class CustomCacheManager(
        private val _evictStrategy: () -> EvictStrategy)
    : CacheManager {
    private val cacheMap = ConcurrentHashMap<String, CustomCache>(16)

    override fun getCacheNames(): Collection<String> {
        return Collections.unmodifiableSet(this.cacheMap.keys)
    }

    override fun getCache(name: String): Cache? {
        var cache: CustomCache? = this.cacheMap[name]
        if (!isValidCache(cache)) {
            synchronized(this.cacheMap) {
                cache = this.cacheMap[name]
                if (!isValidCache(cache)) {
                    cache = CustomCache(name, _evictStrategy())
                    this.cacheMap[name] = cache!!
                }
            }
        }

        return cache
    }

    private fun isValidCache(cache: CustomCache?): Boolean = !(cache == null || cache.evictStrategy.isEvicting)
}