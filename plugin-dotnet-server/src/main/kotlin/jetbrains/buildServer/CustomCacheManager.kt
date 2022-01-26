/*
 * Copyright 2000-2022 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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