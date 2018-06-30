/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.agent.DirectoryCleanersProvider
import jetbrains.buildServer.agent.DirectoryCleanersProviderContext
import jetbrains.buildServer.agent.DirectoryCleanersRegistry
import java.io.File
import java.util.*

/**
 * Cleans up nuget package and cache directories.
 */
class DotnetCacheCleaner : DirectoryCleanersProvider {

    override fun getCleanerName() = "nuget cache cleaner"

    override fun registerDirectoryCleaners(context: DirectoryCleanersProviderContext,
                                           registry: DirectoryCleanersRegistry) {
        System.getenv("NUGET_PACKAGES")?.let { path ->
            val globalPackages = File(path)
            if (globalPackages.isAbsolute) {
                LOG.info("Registering directory $globalPackages for cleaning")
                registry.addCleaner(globalPackages, Date())
            }
        }

        System.getenv("LOCALAPPDATA")?.let { path ->
            val nugetCache = File(path, "NuGet/Cache")
            LOG.info("Registering directory $nugetCache for cleaning")
            registry.addCleaner(nugetCache, Date())

            val nugetv3Cache = File(path, "NuGet/v3-cache")
            LOG.info("Registering directory $nugetv3Cache for cleaning")
            registry.addCleaner(nugetv3Cache, Date())
        }

        System.getProperty("user.home")?.let { path ->
            val nugetPackages = File(path, ".nuget/packages")
            LOG.info("Registering directory $nugetPackages for cleaning")
            registry.addCleaner(nugetPackages, Date())
        }
    }

    companion object {
        private val LOG = Logger.getInstance(DotnetCacheCleaner::class.java.name)
    }
}
