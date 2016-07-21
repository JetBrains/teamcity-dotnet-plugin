/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet

import com.intellij.openapi.diagnostic.Logger

import java.io.File
import java.util.regex.Pattern

/**
 * File utilities
 */
object FileUtils {

    private val LOG = Logger.getInstance(FileUtils::class.java.name)

    /**
     * Returns a first matching file in the list of directories.

     * @param paths   list of directories.
     * *
     * @param pattern pattern.
     * *
     * @return first matching file.
     */
    fun findToolPath(paths: List<String>, pattern: Pattern): String? {
        for (path in paths) {
            val directory = File(path)
            if (!directory.exists()) {
                LOG.debug("Ignoring non existing directory $path")
                continue
            }

            val files = directory.listFiles()
            if (files == null) {
                LOG.debug("Ignoring empty directory $path")
                continue
            }

            for (file in files) {
                val absolutePath = file.absolutePath
                if (pattern.matcher(absolutePath).find()) {
                    return absolutePath
                }
            }
        }

        return null
    }
}
