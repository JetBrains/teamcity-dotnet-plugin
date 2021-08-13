package jetbrains.buildServer.script.discovery

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.dotnet.discovery.SolutionDiscoverImpl
import jetbrains.buildServer.dotnet.discovery.StreamFactory
import kotlinx.coroutines.launch
import java.io.File

class ScriptDiscoverImpl: ScriptDiscover {
    override fun discover(streamFactory: StreamFactory, paths: Sequence<String>) = sequence {
        for (path in paths) {
            LOG.debug("Discover \"$path\"")
            try {
                if("csx".equals(File(path).extension, true)) {
                    yield(Script(path))
                }
            } catch (ex: Exception) {
                LOG.debug("Discover error for \"$path\": ${ex.message}")
            }
        }
    }

    companion object {
        private val LOG: Logger = Logger.getInstance(ScriptDiscoverImpl::class.java.name)
    }
}