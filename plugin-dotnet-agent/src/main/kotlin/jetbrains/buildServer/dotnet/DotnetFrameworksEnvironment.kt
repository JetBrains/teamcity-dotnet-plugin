package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.WindowsRegistryBitness
import java.io.File

interface DotnetFrameworksEnvironment {
    fun tryGetRoot(bitness: WindowsRegistryBitness): File?
}