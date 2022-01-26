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

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.dotNet.DotNetConstants

/**
 * Coverage constants.
 */
object CoverageConstants {
    const val PARAM_TYPE = "dotNetCoverage.tool"

    const val PARAM_DOTCOVER = "dotcover"
    const val PARAM_DOTCOVER_HOME = "dotNetCoverage.dotCover.home.path"
    const val PARAM_DOTCOVER_FILTERS = "dotNetCoverage.dotCover.filters"
    const val PARAM_DOTCOVER_ATTRIBUTE_FILTERS = "dotNetCoverage.dotCover.attributeFilters"
    const val PARAM_DOTCOVER_ARGUMENTS = "dotNetCoverage.dotCover.customCmd"
    const val PARAM_DOTCOVER_LOG_PATH = "teamcity.agent.dotCover.log"

    val DOTNET_FRAMEWORK_PATTERN_3_5 = DotNetConstants.DOTNET_FRAMEWORK_3_5.replace(".", "\\.") + "_.+|" + DotNetConstants.DOTNET_FRAMEWORK_4 + "\\.[\\d\\.]+_.+"
    val DOTNET_FRAMEWORK_PATTERN_4_6_1 = DotNetConstants.DOTNET_FRAMEWORK_4 + "\\.(6\\.(?!0)|[7-9]|[\\d]{2,})[\\d\\.]*_.+"
}
