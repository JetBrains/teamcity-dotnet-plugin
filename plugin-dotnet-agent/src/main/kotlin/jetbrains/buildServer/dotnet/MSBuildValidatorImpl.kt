package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.FileSystemService
import java.io.File

class MSBuildValidatorImpl(
        private val _fileSystemService: FileSystemService)
    : MSBuildValidator {
    override fun isValid(msbuildBasePath: File): Boolean =
            _fileSystemService.isDirectory(msbuildBasePath) && _fileSystemService.isFile(File(msbuildBasePath, "MSBuild.exe"))
}