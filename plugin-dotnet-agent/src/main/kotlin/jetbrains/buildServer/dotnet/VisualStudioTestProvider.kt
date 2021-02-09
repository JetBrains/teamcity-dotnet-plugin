package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.ToolInstanceType
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.runner.*
import org.springframework.cache.annotation.Cacheable
import java.io.File

class VisualStudioTestProvider(
        private val _baseProviders: List<ToolInstanceProvider>,
        private val _visualStudioTestConsoleInstanceFactory: ToolInstanceFactory,
        private val _msTestConsoleInstanceFactory: ToolInstanceFactory,
        private val _parametersService: ParametersService)
    : ToolInstanceProvider {
    @Cacheable("ListOfVisualStuioTest", sync = true)
    override fun getInstances(): Collection<ToolInstance> = (
                    getToolsFromInternalProps() + getTools()
            )
            .distinctBy {
                Pair(Pair(it.toolType, it.platform), Version(it.baseVersion.major, it.baseVersion.minor))
            }
            .toList()

    private fun getTools() =
            _baseProviders
                .asSequence()
                .flatMap { it.getInstances().asSequence() }
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

    private fun getToolsFromInternalProps() =
            _parametersService
                .getParameterNames(ParameterType.Internal)
                .filter { VSTestEnvNameRegex.matches(it) }
                .mapNotNull { _parametersService.tryGetParameter(ParameterType.Internal, it) }
                .mapNotNull {
                    _visualStudioTestConsoleInstanceFactory.tryCreate(File(it), Version.Empty, Platform.Default)
                }

    companion object {
        private val VSTestEnvNameRegex = Regex("^teamcity\\.dotnet\\.vstest\\.[\\d\\.]+\\.install\\.dir$", RegexOption.IGNORE_CASE)
    }
}