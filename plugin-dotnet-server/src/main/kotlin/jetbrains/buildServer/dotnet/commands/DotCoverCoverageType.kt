package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotNet.DotNetConstants
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.CoverageConstants.CROSS_PALTFORM_PATTERN
import jetbrains.buildServer.dotnet.CoverageConstants.DOTCOVER_CROSS_PLATFORM_REQUIREMENT
import jetbrains.buildServer.dotnet.CoverageConstants.DOTNET_FRAMEWORK_PATTERN_3_5
import jetbrains.buildServer.dotnet.CoverageConstants.DOTNET_FRAMEWORK_PATTERN_4_6_1
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementQualifier
import jetbrains.buildServer.requirements.RequirementType
import jetbrains.buildServer.serverSide.InvalidProperty
import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.tools.ServerToolManager
import jetbrains.buildServer.util.VersionComparatorUtil
import org.springframework.beans.factory.BeanFactory

class DotCoverCoverageType: CommandType() {
    override val name: String = CoverageConstants.PARAM_DOTCOVER

    override val description: String = "JetBrains dotCover"

    override val editPage: String = "editDotCoverParameters.jsp"

    override val viewPage: String = "viewDotCoverParameters.jsp"

    override fun validateProperties(properties: Map<String, String>) = sequence {
        yieldAll(super.validateProperties(properties))

        if (properties[CoverageConstants.PARAM_DOTCOVER_HOME].isNullOrBlank()) {
            yield(InvalidProperty(CoverageConstants.PARAM_DOTCOVER_HOME, DotnetConstants.VALIDATION_EMPTY))
        }
    }

    override fun getRequirements(parameters: Map<String, String>, factory: BeanFactory) = sequence {
        yieldAll(super.getRequirements(parameters, factory))

        if (isDocker(parameters)) return@sequence

        val requirements = mutableSetOf<Requirement>()
        requirements += OUR_MINIMAL_REQUIREMENT
        if ("dotcover" != parameters["dotNetCoverage.tool"]) return@sequence
        val dotCoverHomeValue = parameters["dotNetCoverage.dotCover.home.path"] ?: return@sequence
        val toolManager = factory.getBean(ServerToolManager::class.java)
        val toolType = toolManager.findToolType("JetBrains.dotCover.CommandLineTools") ?: return@sequence
        val projectManager = factory.getBean(ProjectManager::class.java)
        val toolVersion = toolManager.resolveToolVersionReference(toolType, dotCoverHomeValue, projectManager.getRootProject())
        if (toolVersion != null) {
            val crossPaltform = toolVersion.version.endsWith("Cross-Platform", true)
            if (crossPaltform) {
                requirements.clear()
                requirements.add(OUR_CROSS_PLATFORM_REQUIREMENT)
            }
            else {
                val dotnet461Based = VersionComparatorUtil.compare("2018.2", toolVersion.getVersion()) <= 0
                if(dotnet461Based) {
                    requirements.clear()
                    requirements.add(OUR_NET_461_REQUIREMENT)
                }
            }
        }

        yieldAll(requirements)
    }

    companion object {
        private val OUR_MINIMAL_REQUIREMENT = Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(" + DOTNET_FRAMEWORK_PATTERN_3_5 + ")", null, RequirementType.EXISTS)
        private val OUR_NET_461_REQUIREMENT = Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(" + DOTNET_FRAMEWORK_PATTERN_4_6_1 + ")", null, RequirementType.EXISTS)
        private val OUR_CROSS_PLATFORM_REQUIREMENT = Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(" + CROSS_PALTFORM_PATTERN + ")", null, RequirementType.EXISTS)
    }
}