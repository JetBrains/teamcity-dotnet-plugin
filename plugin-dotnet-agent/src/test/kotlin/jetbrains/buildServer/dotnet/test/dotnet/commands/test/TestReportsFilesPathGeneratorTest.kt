package jetbrains.buildServer.dotnet.test.dotnet.commands.test

import io.mockk.every
import io.mockk.mockk
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.commands.test.TestReportsFilesPathGenerator
import org.testng.Assert
import org.testng.annotations.Test
import java.io.File
import java.util.regex.Pattern

internal class TestReportsFilesPathGeneratorTest {
    @Test
    fun `test reports file path unique part should not contain path separators or illegal characters`() {
        // Arrange
        val uniquePathPartGenerator = TestReportsFilesPathGenerator.UniquePathPartGenerator()
        val forbiddenCharacters = arrayOf('/', '\\', '<', '>', ':', '"', '|', '?', '*').toCharArray()

        repeat(100000) {
            // Act
            val uniquePathPart = uniquePathPartGenerator.generateUniquePathPart()

            // Assert
            Assert.assertTrue(
                uniquePathPart.indexOfAny(forbiddenCharacters) == -1,
                "Forbidden characters in unique path part $uniquePathPart"
            )
        }
    }

    @Test
    fun `test reports file path should consist of agent temp path, constant part and unique part`() {
        // Arrange
        val agentTempPath = "/agentTmp"
        val pathService = mockk<PathsService> {
            every { getPath(PathType.AgentTemp) } returns File(agentTempPath)
        }

        // Act
        val reportsPath = TestReportsFilesPathGenerator.getTestFilesPath(pathService)

        // Assert
        Assert.assertTrue(Pattern.matches(""".*[/\\]agentTmp[/\\]TestReports[/\\].{22}""", reportsPath.toString()), "actual path is $reportsPath")
    }
}