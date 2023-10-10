/*
 * Copyright 2000-2023 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.CoverageConstants.DOTCOVER_COMPATIBLE_AGENT_PROPERTY_NAME
import jetbrains.buildServer.dotnet.CoverageConstants.DOTCOVER_PACKAGE_ID
import jetbrains.buildServer.dotnet.CoverageConstants.DOTNET_FRAMEWORK_PATTERN_3_5
import jetbrains.buildServer.dotnet.CoverageConstants.DOTNET_FRAMEWORK_4_6_1_PATTERN
import jetbrains.buildServer.dotnet.CoverageConstants.DOTNET_FRAMEWORK_4_7_2_PATTERN
import jetbrains.buildServer.dotnet.CoverageConstants.PARAM_DOTCOVER
import jetbrains.buildServer.dotnet.CoverageConstants.PARAM_DOTCOVER_HOME
import jetbrains.buildServer.dotnet.CoverageConstants.PARAM_TYPE
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.RequirementFactory
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementQualifier
import jetbrains.buildServer.requirements.RequirementType
import jetbrains.buildServer.serverSide.InvalidProperty
import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.tools.ServerToolManager
import jetbrains.buildServer.tools.ToolVersion
import jetbrains.buildServer.util.VersionComparatorUtil
import org.springframework.beans.factory.BeanFactory

class DotCoverCoverageType(requirementFactory: RequirementFactory): CommandType(requirementFactory) {
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

        val toolVersion = getToolVersion(parameters, factory) ?: return@sequence

        // set agent requirement in accordance to dotCover version
        when {
            // Deprecated cross-platform version (with bundled runtime)
            toolVersion.version.endsWith(CoverageConstants.DOTCOVER_CROSS_PLATFORM_DEPRECATED_POSTFIX, true) -> {
                return@sequence     // no requirements since all necessary software bundled with the tool
            }

            // Cross-platform version (without bundled runtime)
            VersionComparatorUtil.compare(toolVersion.version, "2023.3") >= 0 ->
                // no requirements since currently there is no a good way to make composite agent requirements
                // that could express something like: `(Windows AND .NET Framework) OR ((Linux OR macOS) AND .NET SDK)`;
                // in case of imcompatibility of agents a warning will be produced on the build time
                return@sequence

            // Windows-only .NET Framework 4.7.2+ compatible version
            VersionComparatorUtil.compare(toolVersion.version, "2021.2") >= 0 ->
                yield(DOTNET_FRAMEWORK_4_7_2_REQUIREMENT)

            // Windows-only .NET Framework 4.6.1+ compatible version
            VersionComparatorUtil.compare(toolVersion.version, "2018.2") >= 0 ->
                yield(DOTNET_FRAMEWORK_4_6_1_REQUIREMENT)

            // Windows-only .NET Framework 3.5+ compatible version
            VersionComparatorUtil.compare("2018.2", toolVersion.version) > 0 ->
                yield(DOTNET_FRAMEWORK_3_5_REQUIREMENT)
        }
    }

    private fun getToolVersion(parameters: Map<String, String>, factory: BeanFactory): ToolVersion? {
        if (parameters[PARAM_TYPE] != PARAM_DOTCOVER) {
            return null
        }

        val dotCoverHomeValue = parameters[PARAM_DOTCOVER_HOME] ?: return null
        val toolManager = factory.getBean(ServerToolManager::class.java)
        val toolType = toolManager.findToolType(DOTCOVER_PACKAGE_ID) ?: return null
        val projectManager = factory.getBean(ProjectManager::class.java)
        return toolManager.resolveToolVersionReference(toolType, dotCoverHomeValue, projectManager.rootProject)
    }

    companion object {
        private val DOTNET_FRAMEWORK_3_5_REQUIREMENT =
            Requirement("${RequirementQualifier.EXISTS_QUALIFIER}($DOTNET_FRAMEWORK_PATTERN_3_5)", null, RequirementType.EXISTS)

        private val DOTNET_FRAMEWORK_4_6_1_REQUIREMENT =
            Requirement("${RequirementQualifier.EXISTS_QUALIFIER}($DOTNET_FRAMEWORK_4_6_1_PATTERN)", null, RequirementType.EXISTS)

        private val DOTNET_FRAMEWORK_4_7_2_REQUIREMENT =
            Requirement("${RequirementQualifier.EXISTS_QUALIFIER}($DOTNET_FRAMEWORK_4_7_2_PATTERN)", null, RequirementType.EXISTS)

    }
}