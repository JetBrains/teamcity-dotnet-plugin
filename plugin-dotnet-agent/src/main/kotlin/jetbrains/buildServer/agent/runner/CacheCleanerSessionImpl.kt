package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.DirectoryCleanersRegistry
import jetbrains.buildServer.dotnet.DotnetConstants
import org.apache.log4j.Logger
import java.time.Duration
import java.time.Instant
import java.util.*

class CacheCleanerSessionImpl(
        private val _cleaners: List<CacheCleaner>)
    : CacheCleanerSession {

    override fun create(registry: DirectoryCleanersRegistry) {
        val now = Instant.now() //current date
        for (cleaner in _cleaners) {
            val fullName = "${DotnetConstants.CLEANER_NAME} ${cleaner.name} as ${cleaner.type.name}"
            val date = Date.from(now.minus(Duration.ofDays(cleaner.type.weight)))
            LOG.info("Register $fullName.")
            for (target in cleaner.targets) {
                registry.addCleaner(
                        target,
                        date,
                        Runnable {
                            LOG.info("Clearing \"$target\" by $fullName.")
                            try {
                                cleaner.clean(target)
                                LOG.info("Target \"$target\" has been cleared by $fullName.")
                            } catch (error: Throwable) {
                                LOG.error("Target \"$target\" has not been cleared by $fullName.", error)
                            }
                        })
            }
        }
    }

    companion object {
        private val LOG = Logger.getLogger(CacheCleanerSessionImpl::class.java)
    }
}