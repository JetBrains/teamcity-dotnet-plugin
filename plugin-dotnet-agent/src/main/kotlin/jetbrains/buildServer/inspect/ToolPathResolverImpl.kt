package jetbrains.buildServer.inspect

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.inspect.InspectCodeConstants.RUNNER_SETTING_CLT_PLATFORM
import jetbrains.buildServer.inspect.InspectCodeConstants.RUNNER_SETTING_CLT_PLATFORM_X86_PARAMETER
import jetbrains.buildServer.util.OSType
import java.io.File

class ToolPathResolverImpl(
        private val _parametersService: ParametersService,
        private val _virtualContext: VirtualContext)
    : ToolPathResolver {
    override fun resolve(tool: InspectionTool): Path {
        val toolPath = _parametersService.tryGetParameter(ParameterType.Runner, CltConstants.CLT_PATH_PARAMETER)
        if (toolPath == null) {
           throw RunBuildException("Cannot find ${tool.dysplayName}.")
        }

        val executableBase = File(File(toolPath, "tools"), tool.toolName).path;
        val x86 = RUNNER_SETTING_CLT_PLATFORM_X86_PARAMETER.equals(_parametersService.tryGetParameter(ParameterType.Runner, RUNNER_SETTING_CLT_PLATFORM), true)
        return Path(
                when(_virtualContext.targetOSType) {
                    OSType.WINDOWS -> _virtualContext.resolvePath("$executableBase${if(x86) ".x86" else ""}.exe")
                    else -> _virtualContext.resolvePath("$executableBase.sh")
                }
        )
    }
}