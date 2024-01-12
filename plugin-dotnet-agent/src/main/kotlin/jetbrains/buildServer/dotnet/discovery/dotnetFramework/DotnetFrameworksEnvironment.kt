

package jetbrains.buildServer.dotnet.discovery.dotnetFramework

import jetbrains.buildServer.agent.WindowsRegistryBitness
import java.io.File

interface DotnetFrameworksEnvironment {
    fun tryGetRoot(bitness: WindowsRegistryBitness): File?
}