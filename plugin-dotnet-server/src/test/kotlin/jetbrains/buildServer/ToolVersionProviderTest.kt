package jetbrains.buildServer

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.dotnet.Version
import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.serverSide.SProject
import jetbrains.buildServer.tools.ServerToolManager
import jetbrains.buildServer.tools.ToolType
import jetbrains.buildServer.tools.ToolVersion
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class ToolVersionProviderTest {
    @MockK private lateinit var _projectManager: ProjectManager
    @MockK private lateinit var _toolManager: ServerToolManager
    @MockK private lateinit var _toolType: ToolType
    @MockK private lateinit var _rootProject: SProject
    @MockK private lateinit var _toolVersion: ToolVersion

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
        every { _projectManager.rootProject } returns _rootProject
    }

    @Test
    fun shouldProvideToolVersion()
    {
        // Given
        val versionsProvider = createInstance()
        every { _toolManager.findToolType("ToolId") } returns _toolType
        every { _toolManager.resolveToolVersionReference(_toolType, "MyPath", _rootProject) } returns _toolVersion
        every { _toolVersion.version } returns "1.2.3"

        // When
        val actualVersion = versionsProvider.getVersion("MyPath", "ToolId")

        // Then
        Assert.assertEquals(actualVersion, Version(1, 2, 3))
    }

    @Test
    fun shouldNotProvideToolVersionWhenCannotResolveReference()
    {
        // Given
        val versionsProvider = createInstance()
        every { _toolManager.findToolType("ToolId") } returns _toolType
        every { _toolManager.resolveToolVersionReference(_toolType, "MyPath", _rootProject) } returns null

        // When
        val actualVersion = versionsProvider.getVersion("MyPath", "ToolId")

        // Then
        Assert.assertEquals(actualVersion, Version(0))
    }

    @Test
    fun shouldNotProvideToolVersionWhenCannotFindToolType()
    {
        // Given
        val versionsProvider = createInstance()
        every { _toolManager.findToolType("ToolId") } returns null

        // When
        val actualVersion = versionsProvider.getVersion("MyPath", "ToolId")

        // Then
        Assert.assertEquals(actualVersion, Version(0))
    }

    @Test
    fun shouldNotProvideToolVersionWhenPathIsNull()
    {
        // Given
        val versionsProvider = createInstance()

        // When
        val actualVersion = versionsProvider.getVersion(null, "ToolId")

        // Then
        Assert.assertEquals(actualVersion, Version(0))
    }

    private fun createInstance() = ToolVersionProviderImpl(_projectManager, _toolManager)
}