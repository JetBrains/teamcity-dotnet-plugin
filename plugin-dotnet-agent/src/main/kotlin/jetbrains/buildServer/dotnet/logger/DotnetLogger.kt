/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.logger

import jetbrains.buildServer.agent.BuildProgressLogger
import jetbrains.buildServer.agent.runner.ProcessListenerAdapter

/**
 * .NET Core logger.
 */
open class DotnetLogger(private val myLogger: BuildProgressLogger) : ProcessListenerAdapter() {
    override fun onStandardOutput(text: String) {
        myLogger.message(text)
    }

    override fun onErrorOutput(text: String) {
        myLogger.error(text)
    }

    override fun processFinished(exitCode: Int) {
        super.processFinished(exitCode)
    }
}
