package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.DirectoryCleanersRegistry

interface CacheCleanerSession {
    fun create(registry: DirectoryCleanersRegistry)
}