package jetbrains.buildServer.inspect

import jetbrains.buildServer.agent.Path
import java.io.File
import java.io.OutputStream

interface ConfigurationFile {
    fun create(destinationStream: OutputStream, outputFile: Path, cachesHomeDirectory: Path?, debug: Boolean)
}