/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet

/**
 * DotCover runner constants.
 */
object DotCoverConstants {
    const val RUNNER_TYPE = "dotcover"
    const val PARAM_ENABLED = "dotNetCoverage.dotCover.enabled"
    const val PARAM_TOOL_TYPE_ID = "JetBrains.dotCover.CommandLineTools"
    const val PARAM_HOME = "dotNetCoverage.dotCover.home.path"
    const val PARAM_FILTERS = "dotNetCoverage.dotCover.filters"
    const val PARAM_ATTRIBUTE_FILTERS = "dotNetCoverage.dotCover.attributeFilters"
    const val PARAM_ARGUMENTS = "dotNetCoverage.dotCover.customCmd"
}
