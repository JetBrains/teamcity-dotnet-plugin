package jetbrains.buildServer.dotnet.test.inspect

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.E
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.PathMatcher
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.test.agent.runner.ParametersServiceStub
import jetbrains.buildServer.inspect.DupFinderConfigurationFile
import jetbrains.buildServer.inspect.DupFinderConstants
import jetbrains.buildServer.inspect.DupFinderConstants.SETTINGS_DISCARD_COST
import jetbrains.buildServer.inspect.DupFinderConstants.SETTINGS_DISCARD_FIELDS_NAME
import jetbrains.buildServer.inspect.DupFinderConstants.SETTINGS_DISCARD_LITERALS
import jetbrains.buildServer.inspect.DupFinderConstants.SETTINGS_DISCARD_LOCAL_VARIABLES_NAME
import jetbrains.buildServer.inspect.DupFinderConstants.SETTINGS_DISCARD_TYPES
import jetbrains.buildServer.inspect.DupFinderConstants.SETTINGS_EXCLUDE_BY_OPENING_COMMENT
import jetbrains.buildServer.inspect.DupFinderConstants.SETTINGS_EXCLUDE_FILES
import jetbrains.buildServer.inspect.DupFinderConstants.SETTINGS_EXCLUDE_REGION_MESSAGE_SUBSTRINGS
import jetbrains.buildServer.inspect.DupFinderConstants.SETTINGS_INCLUDE_FILES
import jetbrains.buildServer.inspect.DupFinderConstants.SETTINGS_NORMALIZE_TYPES
import jetbrains.buildServer.inspect.XmlWriter
import jetbrains.buildServer.util.OSType
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.ByteArrayOutputStream
import java.io.File

class DupFinderConfigurationFileTest {
    @MockK private lateinit var _xmlWriter: XmlWriter
    @MockK private lateinit var _pathsService: PathsService
    @MockK private lateinit var _pathMatcher: PathMatcher
    @MockK private lateinit var _virtualContext: VirtualContext

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider(name = "createCases")
    fun getCreateCases(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(
                        Path("output.xml"),
                        Path("cache"),
                        true,
                        ParametersServiceStub(mapOf(
                                SETTINGS_DISCARD_FIELDS_NAME to "true",
                                SETTINGS_DISCARD_LITERALS to "True",
                                SETTINGS_DISCARD_LOCAL_VARIABLES_NAME to "true",
                                SETTINGS_DISCARD_TYPES to "true",
                                SETTINGS_NORMALIZE_TYPES to "tRue",
                                SETTINGS_DISCARD_COST to "13",
                                SETTINGS_EXCLUDE_BY_OPENING_COMMENT to "\nexclCom1\nexclCom2\n",
                                SETTINGS_EXCLUDE_REGION_MESSAGE_SUBSTRINGS to "exclReg1\n \nexclReg2",
                                SETTINGS_EXCLUDE_FILES to "excl1\n\nexcl2",
                                SETTINGS_INCLUDE_FILES to "  \nincl1\nincl2\n  "
                        )),
                        OSType.WINDOWS,
                        E("DupFinderOptions",
                                E("ShowStats", "true"),
                                E("ShowText", "true"),
                                E("Debug", "true"),
                                E("DiscardFieldsName", "true"),
                                E("DiscardLiterals", "true"),
                                E("DiscardLocalVariablesName", "true"),
                                E("DiscardTypes", "true"),
                                E("NormalizeTypes", "true"),
                                E("DiscardCost", "13"),
                                E("OutputFile", "output.xml"),
                                E("CachesHomeDirectory", "cache"),
                                E("ExcludeFilesByStartingCommentSubstring",
                                        E("Substring", "exclCom1"),
                                        E("Substring", "exclCom2")),
                                E("ExcludeCodeRegionsByNameSubstring",
                                        E("Substring", "exclReg1"),
                                        E("Substring", "exclReg2")),
                                E("ExcludeFiles",
                                        E("Pattern", "excl1"),
                                        E("Pattern", "excl2")),
                                E("InputFiles",
                                        E("Pattern", "incl1"),
                                        E("Pattern", "incl2"))
                        )
                ),
                arrayOf(
                        Path("output.xml"),
                        Path("cache"),
                        true,
                        ParametersServiceStub(mapOf(
                                SETTINGS_DISCARD_FIELDS_NAME to "true",
                                SETTINGS_DISCARD_LITERALS to "true",
                                SETTINGS_DISCARD_LOCAL_VARIABLES_NAME to "true",
                                SETTINGS_DISCARD_TYPES to "true",
                                SETTINGS_NORMALIZE_TYPES to "true",
                                SETTINGS_DISCARD_COST to "13",
                                SETTINGS_EXCLUDE_BY_OPENING_COMMENT to "exclCom1\n   \nexclCom2",
                                SETTINGS_EXCLUDE_REGION_MESSAGE_SUBSTRINGS to "\nexclReg1\nexclReg2",
                                SETTINGS_EXCLUDE_FILES to "excl1\nexcl2\n  \n \n",
                                SETTINGS_INCLUDE_FILES to "incl1\nincl2\n"
                        )),
                        OSType.UNIX,
                        E("DupFinderOptions",
                                E("ShowStats", "true"),
                                E("ShowText", "true"),
                                E("Debug", "true"),
                                E("DiscardFieldsName", "true"),
                                E("DiscardLiterals", "true"),
                                E("DiscardLocalVariablesName", "true"),
                                E("DiscardTypes", "true"),
                                E("NormalizeTypes", "true"),
                                E("DiscardCost", "13"),
                                E("OutputFile", "output.xml"),
                                E("CachesHomeDirectory", "cache"),
                                E("ExcludeFilesByStartingCommentSubstring",
                                        E("Substring", "exclCom1"),
                                        E("Substring", "exclCom2")),
                                E("ExcludeCodeRegionsByNameSubstring",
                                        E("Substring", "exclReg1"),
                                        E("Substring", "exclReg2")),
                                E("ExcludeFiles",
                                        E("Pattern", "v_excl3"),
                                        E("Pattern", "v_excl4")),
                                E("InputFiles",
                                        E("Pattern", "v_incl3"),
                                        E("Pattern", "v_incl4"))
                        )
                ),
                arrayOf(
                        Path("output.xml"),
                        Path("cache"),
                        true,
                        ParametersServiceStub(mapOf()),
                        OSType.UNIX,
                        E("DupFinderOptions",
                                E("ShowStats", "true"),
                                E("ShowText", "true"),
                                E("Debug", "true"),
                                E("OutputFile", "output.xml"),
                                E("CachesHomeDirectory", "cache")
                        )
                ),
                arrayOf(
                        Path("Output.xml"),
                        null,
                        false,
                        ParametersServiceStub(mapOf()),
                        OSType.UNIX,
                        E("DupFinderOptions",
                                E("ShowStats", "true"),
                                E("ShowText", "true"),
                                E("OutputFile", "Output.xml")
                        )
                )
        )
    }

    @Test(dataProvider = "createCases")
    fun shouldCreate(
            outputFile: Path,
            cachesHomeDirectory: Path?,
            debug: Boolean,
            parametersService: ParametersService,
            os: OSType,
            expctedXml: E?) {
        // Given
        val configFile = createInstance(parametersService)
        val stream = ByteArrayOutputStream()
        var actualXml: E? = null;
        every { _xmlWriter.write(any(), stream) } answers { actualXml = arg<E>(0) }
        every { _virtualContext.targetOSType } returns os
        every { _pathsService.getPath(PathType.WorkingDirectory) } returns File("wd")
        every { _pathMatcher.match(File("wd"), listOf("excl1", "excl2")) } returns listOf(File("excl3"), File("excl4"))
        every { _pathMatcher.match(File("wd"), listOf("incl1", "incl2")) } returns listOf(File("incl3"), File("incl4"))
        every { _virtualContext.resolvePath(File("excl3").absolutePath) } returns "v_excl3"
        every { _virtualContext.resolvePath(File("excl4").absolutePath) } returns "v_excl4"
        every { _virtualContext.resolvePath(File("incl3").absolutePath) } returns "v_incl3"
        every { _virtualContext.resolvePath(File("incl4").absolutePath) } returns "v_incl4"

        // When
        configFile.create(stream, outputFile, cachesHomeDirectory, debug)

        // Then
        Assert.assertEquals(actualXml.toString(), expctedXml.toString())
    }

    private fun createInstance(parametersService: ParametersService) =
            DupFinderConfigurationFile(
                    parametersService,
                    _xmlWriter,
                    _pathsService,
                    _pathMatcher,
                    _virtualContext)
}