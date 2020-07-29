package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.ToolInstanceType
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.runner.ToolInstance
import jetbrains.buildServer.agent.runner.ToolInstanceFactory
import jetbrains.buildServer.agent.runner.ToolInstanceProvider
import org.springframework.cache.annotation.Cacheable

class VisualStudioTestProvider(
        private val _baseProviders: List<ToolInstanceProvider>,
        private val _visualStudioTestConsoleInstanceFactory: ToolInstanceFactory,
        private val _msTestConsoleInstanceFactory: ToolInstanceFactory)
    : ToolInstanceProvider {
    @Cacheable("ListOfVisualStuioTest")
    override fun getInstances() =
            _baseProviders
            .asSequence()
            .flatMap { it.getInstances() }
            .flatMap {
                when(it.toolType) {
                    ToolInstanceType.VisualStudioTest -> sequenceOf(it)
                    ToolInstanceType.MSTest -> sequenceOf(it)
                    ToolInstanceType.VisualStudio -> {
                        sequenceOf(
                                _visualStudioTestConsoleInstanceFactory.tryCreate(it.installationPath, Version.Empty, it.platform),
                                _msTestConsoleInstanceFactory.tryCreate(it.installationPath, Version.Empty, it.platform)
                        )
                    }
                    else -> emptySequence()
                }
            }
            .mapNotNull { it }
}