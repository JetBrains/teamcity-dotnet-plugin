package jetbrains.buildServer.inspect

import jetbrains.buildServer.dotNet.DotNetConstants
import jetbrains.buildServer.inspect.CltConstants.JETBRAINS_RESHARPER_CLT_TOOL_TYPE_ID
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementQualifier
import jetbrains.buildServer.requirements.RequirementType
import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.tools.ServerToolManager
import jetbrains.buildServer.util.VersionComparatorUtil
import java.util.HashSet

class RequirementsProviderImpl(
        private val myProjectManager: ProjectManager,
        private val myToolManager: ServerToolManager) : RequirementsProvider {
    override fun getRequirements(parameters: Map<String, String>): Collection<Requirement> {
        val requirements: MutableSet<Requirement> = HashSet()
        requirements.add(OUR_MINIMAL_REQUIREMENT)
        val path = parameters["jetbrains.resharper-clt.clt-path"]
        if (path != null) {
            val toolType = myToolManager.findToolType(JETBRAINS_RESHARPER_CLT_TOOL_TYPE_ID)
            if (toolType != null) {
                val toolVersion = myToolManager.resolveToolVersionReference(toolType, path, myProjectManager.rootProject)
                if (toolVersion != null) {
                    if (VersionComparatorUtil.compare("2018.2", toolVersion.version) <= 0) {
                        requirements.clear()
                        requirements.add(OUR_NET_461_REQUIREMENT)
                    }
                }
            }
        }
        return requirements
    }

    companion object {
        private val OUR_MINIMAL_REQUIREMENT = Requirement(RequirementQualifier.EXISTS_QUALIFIER + DotNetConstants.DOTNET_FRAMEWORK_4 + "\\..+_.+", null, RequirementType.EXISTS)
        private val OUR_NET_461_REQUIREMENT = Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(" + DotNetConstants.DOTNET_FRAMEWORK_4 + "\\.(6\\.(?!0)|[7-9]|[\\d]{2,})[\\d\\.]*_.+)", null, RequirementType.EXISTS)
    }
}
