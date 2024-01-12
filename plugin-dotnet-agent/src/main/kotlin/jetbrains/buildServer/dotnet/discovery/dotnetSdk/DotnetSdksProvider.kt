

package jetbrains.buildServer.dotnet.discovery.dotnetSdk

import java.io.File

interface DotnetSdksProvider {
     fun getSdks(dotnetExecutable: File): Sequence<DotnetSdk>
}