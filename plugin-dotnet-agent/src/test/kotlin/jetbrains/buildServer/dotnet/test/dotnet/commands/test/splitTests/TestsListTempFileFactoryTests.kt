package jetbrains.buildServer.dotnet.test.dotnet.commands.test.splitTests

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.commands.test.splitTests.TestsListTempFile
import jetbrains.buildServer.dotnet.commands.test.splitTests.TestsListTempFileFactory
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import java.io.File

class TestsListTempFileFactoryTests {
    @MockK
    private lateinit var _pathsServiceMock: PathsService

    @BeforeClass
    fun beforeAll() = MockKAnnotations.init(this)

    @Test
    fun shouldCreateNewTempTestList() {
        // arrange
        val fileMock = mockk<File>()
        every { _pathsServiceMock.getTempFileName(any()) } returns fileMock
        val factory = createFactory()

        // act
        val result = factory.new()

        // assert
        Assert.assertNotNull(result)
        Assert.assertTrue(result is TestsListTempFile)
        verify (exactly = 1) { _pathsServiceMock.getTempFileName(".tests") }
    }

    private fun createFactory() = TestsListTempFileFactory(_pathsServiceMock)
}