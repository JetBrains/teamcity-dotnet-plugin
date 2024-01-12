

package jetbrains.buildServer.inspect

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.inspect.CltConstants.RUNNER_SETTING_CLT_PLATFORM
import jetbrains.buildServer.util.OSType
import java.io.File

class ToolStartInfoResolverImpl(
    private val _parametersService: ParametersService,
    private val _virtualContext: VirtualContext
) : ToolStartInfoResolver {
    override fun resolve(tool: InspectionTool): ToolStartInfo {
        val toolPath = _parametersService.tryGetParameter(ParameterType.Runner, CltConstants.CLT_PATH_PARAMETER)
            ?: throw RunBuildException("Cannot find ${tool.displayName}.")

        val executableBase = File(File(toolPath, "tools"), tool.toolName).path
        val toolPlatform = _parametersService.tryGetParameter(ParameterType.Runner, RUNNER_SETTING_CLT_PLATFORM)
            ?.let { InspectionToolPlatform.tryParse(it) }
            ?: InspectionToolPlatform.WindowsX64

        return when (_virtualContext.targetOSType) {
            OSType.WINDOWS -> {
                when (toolPlatform) {
                    InspectionToolPlatform.WindowsX64 -> ToolStartInfo(Path(_virtualContext.resolvePath(("$executableBase.exe"))), toolPlatform)
                    InspectionToolPlatform.WindowsX86 -> ToolStartInfo(Path(_virtualContext.resolvePath(("$executableBase.x86.exe"))), toolPlatform)
                    else -> ToolStartInfo(
                        Path(""),
                        toolPlatform,
                        listOf(
                            CommandLineArgument("exec"),
                            CommandLineArgument("--runtimeconfig"),
                            CommandLineArgument(_virtualContext.resolvePath("$executableBase.runtimeconfig.json")),
                            CommandLineArgument(_virtualContext.resolvePath(("$executableBase.exe")))
                        )
                    )
                }
            }

            else -> ToolStartInfo(Path(_virtualContext.resolvePath(("$executableBase.sh"))), toolPlatform)
        }
    }
}