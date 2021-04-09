package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.Logger
import org.springframework.cache.annotation.Cacheable

class DotnetFrameworksProviderImpl(
        private val _windowsRegistry: WindowsRegistry,
        private val _registryVisitors: List<DotnetFrameworksWindowsRegistryVisitor>,
        private val _dotnetFrameworkValidator: DotnetFrameworkValidator)
    : DotnetFrameworksProvider {
    @Cacheable("ListOfDotnetFramework", sync = true)
    override fun getFrameworks() =
            _registryVisitors
                    .asSequence()
                    .flatMap { visitor ->
                        visitor.keys.map { key -> Pair(key, visitor) }
                    }
                    .flatMap { (key, visitor) ->
                        _windowsRegistry.accept(key, visitor, true)
                        visitor.getFrameworks()
                    }
                    .map { framework ->
                        val isValid = _dotnetFrameworkValidator.isValid(framework)
                        LOG.debug("Detected ${if (isValid) "valid" else "invalid"} $framework.")
                        Pair(framework, isValid)
                    }
                    .filter { (_, isValid) ->
                        isValid
                    }
                    .map { (framewrok, _) ->
                        framewrok
                    }
                    .distinct()
                    // Materialize to allow caching
                    .toList()
                    .asSequence()

    companion object {
        private val LOG = Logger.getLogger(DotnetFrameworksProviderImpl::class.java)
    }
}