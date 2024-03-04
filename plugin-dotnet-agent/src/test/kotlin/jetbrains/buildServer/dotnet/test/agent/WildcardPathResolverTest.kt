package jetbrains.buildServer.dotnet.test.agent

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.PathsService
import org.testng.Assert.assertEquals
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

internal class WildcardPathResolverTest {
    @MockK private lateinit var _pathsServiceMock: PathsService
    @MockK private lateinit var _pathMatcherMock: PathMatcher
    @MockK private lateinit var _fileSystemServiceMock: FileSystemService
    @MockK private lateinit var _virtualContextMock: VirtualContext
    private lateinit var _instance: WildcardPathResolver

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        clearAllMocks()
        _instance = WildcardPathResolver(
            _pathsServiceMock,
            _pathMatcherMock,
            _fileSystemServiceMock,
            _virtualContextMock,
        )
    }

    public data class TestData(
        public val originalPaths: Sequence<String>,
        public val numberOfWildcardRevealings: Int,
        public val expectedPaths: Sequence<Path>,
    )
    @DataProvider
    public fun `paths with wildcards`() = arrayOf(
        arrayOf(TestData(
            originalPaths = sequenceOf("/should/be/**/*resolved", "should/be/*.resolved", "/already/resolved"),
            numberOfWildcardRevealings = 2,
            expectedPaths = sequenceOf(Path("resolved"), Path("resolved"), Path("resolved")),
        )),
    )
    @Test(dataProvider = "paths with wildcards")
    fun `should resolve all paths when part of them included wildcards`(testData: TestData) {
        // arrange
        val basePath = "basePath"
        every { _pathsServiceMock.getPath(any()) } returns mockk<File>(relaxed = true) {
            every { path } returns basePath
        }
        testData.originalPaths.forEach { originalPath ->
            every { _pathMatcherMock.match(any(), match { it.single() == originalPath }) } returns listOf(mockk<File>(relaxed = true) {
                every { path } returns "/revealedWildcardPath"
            })
            every { _fileSystemServiceMock.isAbsolute(match { it.path == originalPath }) } returns originalPath.startsWith("/")
            every { _fileSystemServiceMock.createFile(any(), any()) } returns mockk<File>(relaxed = true) {
                every { path } returns "/resolvedRelativePath"
            }
        }
        every { _virtualContextMock.resolvePath(any()) } returns "resolved"

        // act
        val actualPaths = _instance.resolve(testData.originalPaths)

        // assert
        assertEquals(actualPaths.count(), testData.expectedPaths.count())
        verify(exactly = testData.numberOfWildcardRevealings) { _pathMatcherMock.match(any(), any()) }
    }
}