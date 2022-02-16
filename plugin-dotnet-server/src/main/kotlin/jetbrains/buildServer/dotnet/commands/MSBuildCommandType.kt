/*
 * Copyright 2000-2022 JetBrains s.r.o.
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

import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementQualifier
import jetbrains.buildServer.requirements.RequirementType
import org.springframework.beans.factory.BeanFactory

/**
 * Provides parameters for dotnet MSBuild command.
 */
class MSBuildCommandType(
        private val _requirementFactory: RequirementFactory)
    : CommandType(_requirementFactory) {
    override val name: String = DotnetCommandType.MSBuild.id

    override val editPage: String = "editMSBuildParameters.jsp"

    override val viewPage: String = "viewMSBuildParameters.jsp"

    override fun getRequirements(parameters: Map<String, String>, factory: BeanFactory) = sequence {
        yieldAll(super.getRequirements(parameters, factory))

        var shouldBeWindows = false
        var hasRequirement = false

        parameters[DotnetConstants.PARAM_MSBUILD_VERSION]?.let {
            Tool.tryParse(it)?.let {
                if (it.type == ToolType.MSBuild) {
                    when (it.platform) {
                        ToolPlatform.Windows -> {
                            shouldBeWindows = true
                            hasRequirement = when (it.bitness) {
                                ToolBitness.X64 -> {
                                    yield(Requirement("MSBuildTools${it.version}.0_x64_Path", null, RequirementType.EXISTS))
                                    true
                                }
                                ToolBitness.X86 -> {
                                    yield(Requirement("MSBuildTools${it.version}.0_x86_Path", null, RequirementType.EXISTS))
                                    true
                                }
                                else -> {
                                    yield(Requirement(RequirementQualifier.EXISTS_QUALIFIER + "MSBuildTools${it.version}\\.0_.+_Path", null, RequirementType.EXISTS))
                                    true
                                }
                            }
                        }
                        ToolPlatform.Mono -> {
                            yield(Requirement(MonoConstants.CONFIG_PATH, null, RequirementType.EXISTS))
                            hasRequirement = true
                        }
                        else -> { }
                    }
                }
            }
        }

        if (!hasRequirement) {
            yield(Requirement(DotnetConstants.CONFIG_SUFFIX_DOTNET_CLI_PATH, null, RequirementType.EXISTS))
        }

        if (shouldBeWindows) {
            yield(Requirement("teamcity.agent.jvm.os.name", "Windows", RequirementType.STARTS_WITH))
        }
    }
}