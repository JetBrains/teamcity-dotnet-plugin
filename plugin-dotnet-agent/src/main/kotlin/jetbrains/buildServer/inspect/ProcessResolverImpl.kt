package jetbrains.buildServer.inspect

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.CommandLineArgumentType
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.inspect.CltConstants.RUNNER_SETTING_CLT_PLATFORM
import jetbrains.buildServer.util.OSType
import java.io.File

class ProcessResolverImpl(
        private val _parametersService: ParametersService,
        private val _virtualContext: VirtualContext)
    : ProcessResolver {
    override fun resolve(tool: InspectionTool): InspectionProcess {
        val toolPath = _parametersService.tryGetParameter(ParameterType.Runner, CltConstants.CLT_PATH_PARAMETER)
        if (toolPath == null) {
           throw RunBuildException("Cannot find ${tool.dysplayName}.")
        }

        val executableBase = File(File(toolPath, "tools"), tool.toolName).path;
        val platform = _parametersService.tryGetParameter(ParameterType.Runner, RUNNER_SETTING_CLT_PLATFORM)
                ?.let { IspectionToolPlatform.tryParse(it) }
                ?: IspectionToolPlatform.X64

        return when(_virtualContext.targetOSType) {
            OSType.WINDOWS ->{
                when(platform) {
                    IspectionToolPlatform.X64 ->  InspectionProcess(Path(_virtualContext.resolvePath(("$executableBase.exe"))))
                    IspectionToolPlatform.X86 ->  InspectionProcess(Path(_virtualContext.resolvePath(("$executableBase.x86.exe"))))
                    else -> InspectionProcess(
                            Path(""),
                            listOf(
                                    CommandLineArgument("exec"),
                                    CommandLineArgument("--runtimeconfig"),
                                    CommandLineArgument(_virtualContext.resolvePath("$executableBase.runtimeconfig.json")),
                                    CommandLineArgument(_virtualContext.resolvePath(("$executableBase.exe")))
                            ))
                }
            }
            else -> InspectionProcess(Path(_virtualContext.resolvePath(("$executableBase.sh"))))
        }
    }
}