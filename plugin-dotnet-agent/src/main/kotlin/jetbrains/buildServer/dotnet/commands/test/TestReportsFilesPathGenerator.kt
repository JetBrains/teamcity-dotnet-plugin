package jetbrains.buildServer.dotnet.commands.test

import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import java.nio.ByteBuffer
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

internal class TestReportsFilesPathGenerator {
    internal class UniquePathPartGenerator
    {
        // Converts UUID to 22-24 character string by encoding the uuid to Base64 and removing the padding
        fun generateUniquePathPart(): String
        {
            val uuid = UUID.randomUUID()
            val byteArray = ByteBuffer.allocate(16)
                .putLong(uuid.mostSignificantBits)
                .putLong(uuid.leastSignificantBits)
                .array()
            return Base64.getUrlEncoder().encodeToString(byteArray).trimEnd('=')
        }
    }

    companion object {
        private val uniquePathPartGenerator = UniquePathPartGenerator()

        fun getTestFilesPath(pathsService: PathsService): Path {
            val uniquePathPart = uniquePathPartGenerator.generateUniquePathPart()
            val agentTempPath = pathsService.getPath(PathType.AgentTemp).canonicalPath
            return Paths.get(agentTempPath, "TestReports", uniquePathPart).toAbsolutePath()
        }
    }
}