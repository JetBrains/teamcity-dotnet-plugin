package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.CommandLine

interface ProgramCommandLineFactory {
    fun create(commandLine: CommandLine): ProgramCommandLine
}