

package jetbrains.buildServer.dotnet.test.dotnet.commands.targeting

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.ParameterType.Runner
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType.WorkingDirectory
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.CommandTarget
import jetbrains.buildServer.dotnet.DotnetConstants.PARAM_EXCLUDED_PATHS
import jetbrains.buildServer.dotnet.DotnetConstants.PARAM_PATHS
import jetbrains.buildServer.dotnet.commands.targeting.TargetService
import jetbrains.buildServer.dotnet.commands.targeting.TargetServiceImpl
import jetbrains.buildServer.util.OSType
import jetbrains.buildServer.util.OSType.*
import org.testng.Assert.assertEquals
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class TargetServiceTest {
    @MockK private lateinit var _pathsService: PathsService
    @MockK private lateinit var _parametersService: ParametersService
    @MockK private lateinit var _argumentsService: ArgumentsService
    @MockK private lateinit var _pathMatcher: PathMatcher
    @MockK private lateinit var _fileSystemService: FileSystemService
    @MockK private lateinit var _virtualContext: VirtualContext

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider
    fun osTypes(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(WINDOWS),
                arrayOf(MAC),
                arrayOf(UNIX)
        )
    }

    @Test(dataProvider = "osTypes")
    fun `should provide targets`(os: OSType) {
        // Given
        val instance = createInstance()
        val checkoutDirectory = File("checkout")
        val includeRules = sequenceOf("rule1", "rule2", "rule3")

        // When
        every { _parametersService.tryGetParameter(Runner, PARAM_PATHS) } returns "some includeRules"
        every { _parametersService.tryGetParameter(Runner, PARAM_EXCLUDED_PATHS) } returns ""
        every { _pathsService.getPath(WorkingDirectory) } returns checkoutDirectory
        every { _argumentsService.split("some includeRules") } returns includeRules
        every { _virtualContext.targetOSType } returns os
        every { _fileSystemService.isAbsolute(any()) } returns false
        every { _virtualContext.resolvePath(any()) } answers { "v_" + File(arg<String>(0)).name }

        val actualTargets = instance.targets.toList()

        // Then
        assertEquals(actualTargets, includeRules.map { CommandTarget(Path("v_${File(it).name}")) }.toList())
    }

    @Test(dataProvider = "osTypes")
    fun `should execute matcher for wildcards`(os: OSType) {
        // Given
        val instance = createInstance()
        val checkoutDirectory = File("checkout")
        val includeRules = sequenceOf("rule1", "rule/**/2", "rul?3")
        val expectedRules = sequenceOf("rule1", "rule/a/2", "rule/b/c/2", "rule3")

        // When
        every { _parametersService.tryGetParameter(Runner, PARAM_PATHS) } returns "some includeRules"
        every { _parametersService.tryGetParameter(Runner, PARAM_EXCLUDED_PATHS) } returns ""
        every { _pathsService.getPath(WorkingDirectory) } returns checkoutDirectory
        every { _argumentsService.split("some includeRules") } returns includeRules
        every { _pathMatcher.match(checkoutDirectory, listOf("rule/**/2")) } returns listOf(File("rule/a/2"), File("rule/b/c/2"))
        every { _pathMatcher.match(checkoutDirectory, listOf("rul?3")) } returns listOf(File("rule3"))
        every { _fileSystemService.isAbsolute(any()) } returns false
        every { _virtualContext.targetOSType } returns os
        every { _virtualContext.resolvePath(any()) } answers { "v_" + File(arg<String>(0)).name }

        val actualTargets = instance.targets.toList()

        // Then
        assertEquals(actualTargets, expectedRules.map { CommandTarget(Path("v_${File(it).name}")) }.toList())
    }

    @Test(dataProvider = "osTypes")
    fun `should throw RunBuildException when targets were not matched`(os: OSType) {
        // Given
        val instance = createInstance()
        val checkoutDirectory = File("checkout")
        val includeRules = listOf("rule1", "rule/**/2", "rule3")

        // When
        every { _parametersService.tryGetParameter(Runner, PARAM_PATHS) } returns "some includeRules"
        every { _parametersService.tryGetParameter(Runner, PARAM_EXCLUDED_PATHS) } returns ""
        every { _pathsService.getPath(WorkingDirectory) } returns checkoutDirectory
        every { _argumentsService.split("some includeRules") } returns includeRules.asSequence()
        every { _pathMatcher.match(checkoutDirectory, listOf("rule/**/2")) } returns emptyList<File>()
        every { _fileSystemService.isAbsolute(any()) } returns false
        every { _virtualContext.targetOSType } returns os
        every { _virtualContext.resolvePath(any()) } answers { "v_" + File(arg<String>(0)).name }

        var actualExceptionWasThrown = false
        try {
            instance.targets.toList()
        } catch (ex: RunBuildException) {
            actualExceptionWasThrown = true
        }

        // Then
        assertEquals(actualExceptionWasThrown, true)
    }

    @DataProvider
    fun `empty paths param`(): Array<Array<out String?>> {
        return arrayOf(
                arrayOf(""),
                arrayOf("  "),
                arrayOf(null as String?))
    }

    @Test(dataProvider = "empty paths param")
    fun `should provide empty targets sequence when paths param is empty`(pathsParam: String?) {
        // Given
        val instance = createInstance()
        val checkoutDirectory = File("checkout")

        // When
        every { _parametersService.tryGetParameter(Runner, PARAM_PATHS) } returns pathsParam
        every { _parametersService.tryGetParameter(Runner, PARAM_EXCLUDED_PATHS) } returns ""
        every { _pathsService.getPath(WorkingDirectory) } returns checkoutDirectory

        val actualTargets = instance.targets.toList()

        // Then
        assertEquals(actualTargets, emptyList<CommandTarget>())
    }

    @Test(dataProvider = "osTypes")
    fun `should provide targets when excluded paths are specified`(os: OSType) {
        // Given
        val instance = createInstance()
        val checkoutDirectory = File("checkout")
        val includeRules = sequenceOf("rule1", "rule2", "rule3")
        val excludeRules = sequenceOf("rule3")
        val expectedTargets = sequenceOf("rule1", "rule2")

        // When
        every { _parametersService.tryGetParameter(Runner, PARAM_PATHS) } returns "some includeRules"
        every { _parametersService.tryGetParameter(Runner, PARAM_EXCLUDED_PATHS) } returns "some excludeRules"
        every { _pathsService.getPath(WorkingDirectory) } returns checkoutDirectory
        every { _argumentsService.split("some includeRules") } returns includeRules
        every { _argumentsService.split("some excludeRules") } returns excludeRules
        every { _virtualContext.targetOSType } returns os
        every { _fileSystemService.isAbsolute(any()) } returns false
        every { _virtualContext.resolvePath(any()) } answers { "v_" + File(arg<String>(0)).name }

        val actualTargets = instance.targets.toList()

        // Then
        assertEquals(actualTargets, expectedTargets.map { CommandTarget(Path("v_${File(it).name}")) }.toList())
    }

    @DataProvider
    fun `included and excluded paths with wildcards`(): Array<Array<out Any?>> = arrayOf(
        arrayOf(
            mapOf(
                "/d1/d2/*.dll" to listOf("/d1/d2/f1.dll", "/d1/d2/f2.dll", "/d1/d2/f3.dll"),
                "/d1/d3/*.dll" to listOf("/d1/d3/f1.dll", "/d1/d3/f2.dll")
            ),
            mapOf("/d1/*/f1.dll" to listOf("/d1/d2/f1.dll", "/d1/d3/f1.dll")),
            listOf("/d1/d2/f2.dll", "/d1/d2/f3.dll", "/d1/d3/f2.dll")
        ),
        arrayOf(
            mapOf("**.dll" to listOf("f.dll", "/d1/d2/f2.dll", "/d1/d2/d3/f3.dll")),
            mapOf("**f?.dll" to listOf("/d1/d2/f2.dll", "/d1/d2/d3/f3.dll")),
            listOf("f.dll")
        ),
        arrayOf(
            mapOf("*tests.dll" to listOf("1_tests.dll", "2_tests.dll", "system_tests.dll")),
            mapOf("**system**.dll" to listOf("system_tests.dll", "system_another_file.dll")),
            listOf("1_tests.dll", "2_tests.dll")
        ),
        arrayOf(
            mapOf("**.dll" to listOf("f1.dll", "f2.dll")),
            mapOf("" to emptyList<String>()),
            listOf("f1.dll", "f2.dll")
        ),
    )

    @Test(dataProvider = "included and excluded paths with wildcards")
    fun `should provide targets when excluded paths with wildcards are specified`(
        includedRules: Map<String, List<String>>,
        excludedRules: Map<String, List<String>>,
        expectedTargets: List<String>
    ) {
        // Given
        val instance = createInstance()
        val checkoutDirectory = File("checkout")

        // When
        every { _parametersService.tryGetParameter(Runner, PARAM_PATHS) } returns "some includeRules"
        every { _parametersService.tryGetParameter(Runner, PARAM_EXCLUDED_PATHS) } returns "some excludedRules"
        every { _pathsService.getPath(WorkingDirectory) } returns checkoutDirectory
        every { _argumentsService.split("some includeRules") } returns includedRules.keys.asSequence()
        every { _argumentsService.split("some excludedRules") } returns excludedRules.keys.asSequence()
        includedRules.forEach { (key, value) ->
            every { _pathMatcher.match(checkoutDirectory, listOf(key)) } returns value.map { File(it) }.toList()
        }
        excludedRules.forEach { (key, value) ->
            every { _pathMatcher.match(checkoutDirectory, listOf(key)) } returns value.map { File(it) }.toList()
        }
        every { _fileSystemService.isAbsolute(any()) } returns false
        every { _virtualContext.targetOSType } returns WINDOWS
        every { _virtualContext.resolvePath(any()) } answers { "v_" + File(arg<String>(0)).path }

        val actualTargets = instance.targets.toList()

        // Then
        assertEquals(
            actualTargets,
            expectedTargets.map { CommandTarget(Path("v_${File(checkoutDirectory, it).path}")) }.toList()
        )
    }

    @DataProvider
    fun `empty excluded paths param`(): Array<Array<out String?>> {
        return arrayOf(
            arrayOf(""),
            arrayOf("  "),
            arrayOf(null as String?))
    }

    @Test(dataProvider = "empty excluded paths param")
    fun `should provide targets when excluded paths parameter is empty or null`(excludedPathsParam: String?) {
        // Given
        val instance = createInstance()
        val checkoutDirectory = File("checkout")
        val includeRules = sequenceOf("rule1", "rule2", "rule3")
        val expectedTargets = sequenceOf("rule1", "rule2", "rule3")

        // When
        every { _parametersService.tryGetParameter(Runner, PARAM_PATHS) } returns "some includeRules"
        every { _parametersService.tryGetParameter(Runner, PARAM_EXCLUDED_PATHS) } returns excludedPathsParam
        every { _pathsService.getPath(WorkingDirectory) } returns checkoutDirectory
        every { _argumentsService.split("some includeRules") } returns includeRules
        every { _virtualContext.targetOSType } returns WINDOWS
        every { _fileSystemService.isAbsolute(any()) } returns false
        every { _virtualContext.resolvePath(any()) } answers { "v_" + File(arg<String>(0)).name }

        val actualTargets = instance.targets.toList()

        // Then
        assertEquals(actualTargets, expectedTargets.map { CommandTarget(Path("v_${File(it).name}")) }.toList())
    }

    private fun createInstance(): TargetService {
        return TargetServiceImpl(
                _pathsService,
                _parametersService,
                _argumentsService,
                _pathMatcher,
                _fileSystemService,
                _virtualContext)
    }
}