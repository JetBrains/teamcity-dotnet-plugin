/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet

import java.util.*

/**
 * .NET utilities.
 */
object DotnetUtils {

    /**
     * Update process environment.

     * @param env environment.
     * *
     * @return updated environment.
     */
    fun updateEnvironment(env: Map<String, String>): Map<String, String> {

        // disable telemetry data collection: https://aka.ms/dotnet-cli-telemetry
        val environment = HashMap(env)
        environment["DOTNET_CLI_TELEMETRY_OPTOUT"] = "true"

        // disable showing eula and first time package caching
        environment["DOTNET_SKIP_FIRST_TIME_EXPERIENCE"] = "true"

        // skip xml docs download for restored packages
        environment["NUGET_XMLDOC_MODE"] = "skip"

        return environment
    }
}
