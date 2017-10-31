package jetbrains.buildServer.dotnet.discovery

import com.intellij.openapi.diagnostic.Logger
import java.lang.Thread.yield
import kotlin.coroutines.experimental.buildSequence

class SolutionDiscoverImpl(private val _discoverers: List<SolutionDeserializer>) : SolutionDiscover {
    override fun discover(streamFactory: StreamFactory, paths: Sequence<String>): Sequence<Solution> = buildSequence {
        for (path in paths) {
            LOG.debug("Discover \"$path\"")
            for (discoverer in _discoverers) {
                if (!discoverer.accept(path)) {
                    continue
                }

                LOG.debug("Use discoverer \"${discoverer}\" for \"$path\"")
                try {
                    val solution = discoverer.deserialize(path, streamFactory)
                    LOG.debug("\"${discoverer}\" finds \"${solution}\"")
                    yield(solution)
                    break
                }
                catch (ex: Exception) {
                    LOG.error(ex)
                }
            }
        }
    }

    companion object {
        private val LOG: Logger = Logger.getInstance(SolutionDiscoverImpl::class.java.name)
    }
}