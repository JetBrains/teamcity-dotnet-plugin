package jetbrains.buildServer.dotnet.test.visualStudio

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.JsonParser
import jetbrains.buildServer.agent.ToolInstanceType
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.visualStudio.JsonVisualStudioInstanceParser
import jetbrains.buildServer.visualStudio.JsonVisualStudioInstanceParser.CatalogInfo
import jetbrains.buildServer.visualStudio.JsonVisualStudioInstanceParser.Companion.TeamExplorerProductId
import jetbrains.buildServer.visualStudio.JsonVisualStudioInstanceParser.ProductInfo
import jetbrains.buildServer.visualStudio.JsonVisualStudioInstanceParser.VisualStudioState
import jetbrains.buildServer.agent.runner.ToolInstance
import jetbrains.buildServer.dotnet.Platform
import jetbrains.buildServer.visualStudio.VisualStudioInstanceParser
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.ByteArrayInputStream
import java.io.File


class VisualStudioInstanceParserTest {
    @MockK private lateinit var _jsonParser: JsonParser
    private val _path = File(File(File("path"), "Common7"), "IDE")

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider
    fun getParseData(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(
                        VisualStudioState().let {
                            it.installationPath = "path"
                            it.catalogInfo = CatalogInfo()
                            it.catalogInfo!!.productDisplayVersion = "17.0.0-Preview-2.1"
                            it.catalogInfo!!.productLineVersion = "2022"
                            it.product = ProductInfo()
                            it.product!!.id = "abc"
                            it
                        },
                        ToolInstance(ToolInstanceType.VisualStudio, _path, Version(17, 0, 0, "Preview-2.1"), Version(2022), Platform.Default)
                ),
                arrayOf(
                        VisualStudioState().let {
                            it.installationPath = "path"
                            it.catalogInfo = CatalogInfo()
                            it.catalogInfo!!.productDisplayVersion = "16.6.3"
                            it.catalogInfo!!.productLineVersion = "2019"
                            it.product = ProductInfo()
                            it.product!!.id = "abc"
                            it
                        },
                        ToolInstance(ToolInstanceType.VisualStudio, _path, Version(16, 6, 3), Version(2019), Platform.Default)
                ),
                arrayOf(
                        VisualStudioState().let {
                            it.installationPath = "path"
                            it.catalogInfo = CatalogInfo()
                            it.installationVersion = "instalation"
                            it.catalogInfo!!.productDisplayVersion = null
                            it.catalogInfo!!.productLineVersion = "2019"
                            it.product = ProductInfo()
                            it.product!!.id = "abc"
                            it
                        },
                        ToolInstance(ToolInstanceType.VisualStudio, _path, Version.Empty, Version(2019), Platform.Default)
                ),
                arrayOf(
                        VisualStudioState().let {
                            it.installationPath = "path"
                            it.catalogInfo = CatalogInfo()
                            it.catalogInfo!!.productDisplayVersion = "16.6.3"
                            it.catalogInfo!!.productLineVersion = "2019"
                            it.product = ProductInfo()
                            it
                        },
                        ToolInstance(ToolInstanceType.VisualStudio, _path, Version(16, 6, 3), Version(2019), Platform.Default)
                ),
                arrayOf(
                        VisualStudioState().let {
                            it.installationPath = "path"
                            it.catalogInfo = CatalogInfo()
                            it.catalogInfo!!.productDisplayVersion = "16.6.3"
                            it.catalogInfo!!.productLineVersion = "2019"
                            it.product = ProductInfo()
                            it.product!!.id = TeamExplorerProductId
                            it
                        },
                        null
                ),
                arrayOf(
                        VisualStudioState().let {
                            it.installationPath = null
                            it.catalogInfo = CatalogInfo()
                            it.catalogInfo!!.productDisplayVersion = "16.6.3"
                            it.catalogInfo!!.productLineVersion = "2019"
                            it.product = ProductInfo()
                            it.product!!.id = "abc"
                            it
                        },
                        null
                ),
                arrayOf(
                        VisualStudioState().let {
                            it.installationPath = "path"
                            it.catalogInfo = CatalogInfo()
                            it.catalogInfo!!.productDisplayVersion = null
                            it.catalogInfo!!.productLineVersion = "2019"
                            it.product = ProductInfo()
                            it.product!!.id = "abc"
                            it
                        },
                        null
                ),
                arrayOf(
                        VisualStudioState().let {
                            it.installationPath = "path"
                            it.catalogInfo = CatalogInfo()
                            it.catalogInfo!!.productDisplayVersion = "16.6.3"
                            it.catalogInfo!!.productLineVersion = null
                            it.product = ProductInfo()
                            it.product!!.id = "abc"
                            it
                        },
                        null
                ),
                arrayOf(
                        VisualStudioState().let {
                            it.installationPath = "path"
                            it.product = ProductInfo()
                            it.product!!.id = "abc"
                            it
                        },
                        null
                ),
                arrayOf(
                        VisualStudioState(),
                        null
                )
        )
    }

    @Test(dataProvider = "getParseData")
    fun shouldParse(visualStudioState: VisualStudioState, expectedInstance: ToolInstance?) {
        // Given
        val parser = createInstance()
        every { _jsonParser.tryParse<VisualStudioState>(any(), VisualStudioState::class.java) } returns visualStudioState

        // When
        val actualInstance= parser.tryParse(ByteArrayInputStream(byteArrayOf(0, 1, 2)))

        // Then
        Assert.assertEquals(actualInstance, expectedInstance)
    }

    private fun createInstance(): VisualStudioInstanceParser {
        return JsonVisualStudioInstanceParser(_jsonParser)
    }
}