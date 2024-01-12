

package jetbrains.buildServer.dotnet.discovery

import com.intellij.openapi.diagnostic.Logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class SolutionDiscoverImpl(
        private val _dispatcher: CoroutineDispatcher,
        private val _discoverers: List<SolutionDeserializer>)
    : SolutionDiscover {

    override fun discover(streamFactory: StreamFactory, paths: Sequence<String>): Collection<Solution> =
            runBlocking {
                val solutions = mutableListOf<Solution>()
                for (path in paths) {
                    LOG.debug("Discover \"$path\"")
                    for (discoverer in _discoverers) {
                        if (!discoverer.isAccepted(path)) {
                            continue
                        }

                        LOG.debug("Use discoverer \"$discoverer\" for \"$path\"")
                        try {
                            launch(_dispatcher) {
                                val solution = discoverer.deserialize(path, streamFactory)
                                LOG.debug("\"$discoverer\" finds \"$solution\"")
                                synchronized(solution) {
                                    solutions.add(solution)
                                }
                            }

                            break
                        } catch (ex: Exception) {
                            LOG.debug("Discover error for \"$path\": ${ex.message}")
                        }
                    }
                }

                solutions
            }

    companion object {
        private val LOG: Logger = Logger.getInstance(SolutionDiscoverImpl::class.java.name)
    }
}