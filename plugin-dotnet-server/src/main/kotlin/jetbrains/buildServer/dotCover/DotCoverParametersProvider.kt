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

package jetbrains.buildServer.dotCover

import jetbrains.buildServer.CommandType
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.CoverageConstants.PARAM_DOTCOVER_HOME
import jetbrains.buildServer.dotnet.commands.*
import jetbrains.buildServer.dotnet.discovery.SdkResolverImpl
import jetbrains.buildServer.web.functions.InternalProperties

/**
 * Provides parameters for dotnet runner.
 */
class DotCoverParametersProvider {

//    val commands: Collection<CommandType> = commandTypes.values
//    val coverages: Collection<CommandType> = coverageTypes.values

    // Command parameters

    val commandKey = "command"

    val argumentsKey = "args"

    val dotCoverHomeKey = PARAM_DOTCOVER_HOME

    val dotCoverFiltersKey = CoverageConstants.PARAM_DOTCOVER_FILTERS

    val dotCoverAttributeFiltersKey = CoverageConstants.PARAM_DOTCOVER_ATTRIBUTE_FILTERS

    val dotCoverArgumentsKey: String
        get() = CoverageConstants.PARAM_DOTCOVER_ARGUMENTS

//    companion object {
//        private val requirementFactory: RequirementFactory = RequirementFactoryImpl(SdkResolverImpl(SdkTypeResolverImpl()))
//        val commandTypes get() = sequenceOf
//            RestoreCommandType(requirementFactory),
//            BuildCommandType(requirementFactory),
//            TestCommandType(requirementFactory),
//            PublishCommandType(requirementFactory),
//            PackCommandType(requirementFactory),
//            NugetPushCommandType(requirementFactory),
//            NugetDeleteCommandType(requirementFactory),
//            CleanCommandType(requirementFactory),
//            RunCommandType(requirementFactory),
//            MSBuildCommandType(requirementFactory),
//            VSTestCommandType(requirementFactory),
//            VisualStudioCommandType(requirementFactory)
//        )
//            .plus(if(experimentalMode) experimentalCommandTypes else emptySequence())
//            .sortedBy { it.description }
//            .plus(CustomCommandType(requirementFactory))
//            .associateBy { it.name }
//
//        val coverageTypes get() = sequenceOf<CommandType>(DotCoverCoverageType(requirementFactory))
//            .sortedBy { it.name }
//            .associateBy { it.name }
//    }
}
