package jetbrains.buildServer.script

import jetbrains.buildServer.agent.CommandLine
import java.io.File

interface CommandLineFactory {
    fun create(): CommandLine
}