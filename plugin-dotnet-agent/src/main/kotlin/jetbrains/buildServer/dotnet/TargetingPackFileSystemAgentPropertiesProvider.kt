package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.ToolInstanceType
import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_PREFIX_DOTNET_FRAMEWORK_TARGETING_PACK
import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_SUFFIX_PATH
import jetbrains.buildServer.agent.Logger
import java.io.File

class TargetingPackFileSystemAgentPropertiesProvider(
        private val _environment: Environment,
        private val _fileSystemService: FileSystemService)
    : AgentPropertiesProvider {

    override val desription = "Dotnet Framework targeting pack in file system"

    override val properties get() =
        ProgramFilesEnvVars
                .map { _environment.tryGetVariable(it) }
                .filterNotNull()
                .flatMap {
                    LOG.debug("Starts at \"$it\".")
                    sequence {
                        val basePath = File(File(File(it, "Reference Assemblies"), "Microsoft"), "Framework")
                        yield(basePath)
                        yield(File(basePath, ".NETFramework"))
                    }
                }
                .filter { _fileSystemService.isExists(it) }
                // C:\Program Files (x86)\Reference Assemblies\Microsoft\Framework
                // C:\Program Files (x86)\Reference Assemblies\Microsoft\Framework\.NETFramework
                .map {
                    LOG.debug("Goes through \"$it\".")
                    it
                }
                .filter { _fileSystemService.isDirectory(it) }
                .flatMap {  _fileSystemService.list(it) }
                .filter { _fileSystemService.isDirectory(it) }
                .filter { TargetPackVersionPatter.containsMatchIn(it.name) }
                // C:\Program Files (x86)\Reference Assemblies\Microsoft\Framework\v3.5
                // C:\Program Files (x86)\Reference Assemblies\Microsoft\Framework\.NETFramework\v4.8
                .filter {
                    LOG.debug("Goes through \"$it\".")
                    val frameworkList = File(File(it, "RedistList"), "FrameworkList.xml")
                    // check file C:\Program Files (x86)\Reference Assemblies\Microsoft\Framework\.NETFramework\v4.8\RedistList\FrameworkList.xml
                    // or name C:\Program Files (x86)\Reference Assemblies\Microsoft\Framework\.NETFramework\v4.X
                    it.name.toLowerCase().endsWith("x") || (_fileSystemService.isExists(frameworkList) && _fileSystemService.isFile(frameworkList))
                }
                .distinctBy { it.name.toLowerCase() }
                .map {
                    LOG.debug("Goes through \"$it\".")
                    val version = it.name.substring(1)
                    LOG.debug("Found .NET Framework targeting pack $version at \"${it.path}\".")
                    AgentProperty(ToolInstanceType.TargetingPack, "$CONFIG_PREFIX_DOTNET_FRAMEWORK_TARGETING_PACK${version}$CONFIG_SUFFIX_PATH", it.path)
                }

    companion object {
        private val LOG = Logger.getLogger(TargetingPackFileSystemAgentPropertiesProvider::class.java)
        private val ProgramFilesEnvVars = sequenceOf("ProgramFiles(x86)", "ProgramW6432")
        private val TargetPackVersionPatter = Regex("^v\\d+\\.[\\d\\.x]+$", RegexOption.IGNORE_CASE)
    }
}