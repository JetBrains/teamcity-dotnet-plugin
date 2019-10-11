package jetbrains.buildServer

import jetbrains.buildServer.tools.GetPackageVersionResult
import jetbrains.buildServer.tools.ToolType
import jetbrains.buildServer.tools.ToolVersion
import java.io.File

interface ToolService {
    fun getTools(toolType: ToolType, vararg packageIds: String): List<NuGetTool>

    fun tryGetPackageVersion(toolType: ToolType, toolPackage: File): GetPackageVersionResult?

    fun fetchToolPackage(toolType: ToolType, toolVersion: ToolVersion, targetDirectory: File): File

    fun unpackToolPackage(toolType: ToolType, toolPackage: File, nugetPackageDirectory: String, targetDirectory: File)
}