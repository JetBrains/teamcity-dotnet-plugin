package jetbrains.buildServer.script

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.including
import jetbrains.buildServer.to
import java.io.File

interface AnyVersionResolver {
    fun resolve(toolPath: File) : File
}