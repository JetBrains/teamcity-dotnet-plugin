

package jetbrains.buildServer

import jetbrains.buildServer.dotnet.SemanticVersion
import jetbrains.buildServer.tools.GetPackageVersionResult
import jetbrains.buildServer.tools.ToolType
import jetbrains.buildServer.tools.ToolVersion
import java.io.File

interface ToolService {
    fun getTools(toolType: ToolType, vararg packageIds: String): List<NuGetTool>

    fun getPackages(vararg packageIds: String): List<NuGetPackage>

    fun tryGetPackageVersion(toolType: ToolType, toolPackage: File, vararg packageIds: String): GetPackageVersionResult?

    fun getPackageVersion(toolPackage: File, vararg packageIds: String): SemanticVersion?

    fun fetchToolPackage(toolType: ToolType, toolVersion: ToolVersion, targetDirectory: File, vararg packageIds: String): File

    fun fetchToolPackage(toolVersion: ToolVersion, targetDirectory: File, downloadUrl: String, destinationFileName: String): File

    fun unpackToolPackage(toolPackage: File, packageDirectory: String, targetDirectory: File, vararg packageIds: String)
}