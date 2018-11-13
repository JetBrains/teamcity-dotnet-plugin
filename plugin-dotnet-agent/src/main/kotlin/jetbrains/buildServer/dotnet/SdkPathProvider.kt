package jetbrains.buildServer.dotnet

import java.io.File

interface SdkPathProvider {
    val path: File
}