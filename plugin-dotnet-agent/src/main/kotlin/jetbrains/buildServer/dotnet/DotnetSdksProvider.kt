package jetbrains.buildServer.dotnet

import java.io.File

interface DotnetSdksProvider {
     fun getSdks(dotnetExecutable: File): Sequence<DotnetSdk>
}