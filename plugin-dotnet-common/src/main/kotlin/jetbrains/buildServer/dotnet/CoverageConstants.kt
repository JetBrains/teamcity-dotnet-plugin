/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
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

    const val DOTCOVER_CROSS_PLATFORM_REQUIREMENT = "DotCoverCrossPlatform"
    val DOTNET_FRAMEWORK_PATTERN_3_5 = DotNetConstants.DOTNET_FRAMEWORK_3_5.replace(".", "\\.") + "_.+|" + DotNetConstants.DOTNET_FRAMEWORK_4 + "\\.[\\d\\.]+_.+"
    val DOTNET_FRAMEWORK_PATTERN_4_6_1 = DotNetConstants.DOTNET_FRAMEWORK_4 + "\\.(6\\.(?!0)|[7-9]|[\\d]{2,})[\\d\\.]*_.+"
    val CROSS_PALTFORM_PATTERN = DOTCOVER_CROSS_PLATFORM_REQUIREMENT + ".*"
}
