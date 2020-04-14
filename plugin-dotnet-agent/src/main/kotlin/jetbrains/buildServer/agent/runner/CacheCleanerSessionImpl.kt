package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.DirectoryCleanersProvider
import jetbrains.buildServer.agent.DirectoryCleanersProviderContext
import jetbrains.buildServer.agent.DirectoryCleanersRegistry
import jetbrains.buildServer.dotnet.DotnetConstants
import org.apache.log4j.Logger
import java.io.File
import java.time.Duration
import java.time.Instant
import java.util.*

class CacheCleanerSessionImpl(
        private val _cleaners: List<CacheCleaner>)
    : DirectoryCleanersProvider {

    override fun getCleanerName() = DotnetConstants.CLEANER_NAME

    override fun registerDirectoryCleaners(context: DirectoryCleanersProviderContext, registry: DirectoryCleanersRegistry) {
        val now = Instant.now()
        val targets = mutableSetOf<File>()
        for (cleaner in _cleaners) {
            val fullName = "${DotnetConstants.CLEANER_NAME} ${cleaner.name}"
            val date = Date.from(now.minus(Duration.ofDays(cleaner.type.weight)))
            LOG.info("Register $fullName.")
            for (target in cleaner.targets) {
                if (targets.add(target)) {
                    registry.addCleaner(
                            target,
                            date,
                            Runnable {
                                LOG.info("Clearing \"$target\" by $fullName.")
                                try {
                                    if (cleaner.clean(target)) {
                                        LOG.info("Target \"$target\" has been cleared by $fullName.")
                                    } else {
                                        LOG.warn("Target \"$target\" has not been cleared by $fullName.")                                    }
                                } catch (error: Throwable) {
                                    LOG.error("Target \"$target\" has not been cleared by $fullName.", error)
                                }
                            })

                    LOG.info("\"$target\" added for cleaning by $fullName.")
                } else {
                    LOG.info("\"$target\" already added for cleaning.")
                }
            }
        }
    }

    companion object {
        private val LOG = Logger.getLogger(CacheCleanerSessionImpl::class.java)
    }
}