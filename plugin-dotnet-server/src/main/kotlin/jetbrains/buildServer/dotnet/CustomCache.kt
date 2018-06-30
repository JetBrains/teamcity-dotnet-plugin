package jetbrains.buildServer.dotnet

import org.springframework.cache.concurrent.ConcurrentMapCache

class CustomCache(name: String, val evictStrategy: EvictStrategy) : ConcurrentMapCache(name, true)