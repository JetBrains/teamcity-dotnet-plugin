package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.JsonParser
import jetbrains.buildServer.dotnet.CommandResult
import jetbrains.buildServer.dotnet.JsonVisualStudioInstanceParser
import jetbrains.buildServer.dotnet.JsonVisualStudioInstanceParser.CatalogInfo
import jetbrains.buildServer.dotnet.JsonVisualStudioInstanceParser.Companion.TeamExplorerProductId
import jetbrains.buildServer.dotnet.JsonVisualStudioInstanceParser.ProductInfo
import jetbrains.buildServer.dotnet.JsonVisualStudioInstanceParser.VisualStudioState
import jetbrains.buildServer.dotnet.VisualStudioInstance
import jetbrains.buildServer.dotnet.VisualStudioInstanceParser
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.Serializable
import java.util.*


class VisualStudioInstanceParserTest {
    @MockK private lateinit var _jsonParser: JsonParser

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
                            it.catalogInfo!!.productDisplayVersion = "display"
                            it.catalogInfo!!.productLineVersion = "product"
                            it.product = ProductInfo()
                            it.product!!.id = "abc"
                            it
                        },
                        VisualStudioInstance("path", "display", "product")
                ),
                arrayOf(
                        VisualStudioState().let {
                            it.installationPath = "path"
                            it.catalogInfo = CatalogInfo()
                            it.installationVersion = "instalation"
                            it.catalogInfo!!.productDisplayVersion = null
                            it.catalogInfo!!.productLineVersion = "product"
                            it.product = ProductInfo()
                            it.product!!.id = "abc"
                            it
                        },
                        VisualStudioInstance("path", "instalation", "product")
                ),
                arrayOf(
                        VisualStudioState().let {
                            it.installationPath = "path"
                            it.catalogInfo = CatalogInfo()
                            it.catalogInfo!!.productDisplayVersion = "display"
                            it.catalogInfo!!.productLineVersion = "product"
                            it.product = ProductInfo()
                            it
                        },
                        VisualStudioInstance("path", "display", "product")
                ),
                arrayOf(
                        VisualStudioState().let {
                            it.installationPath = "path"
                            it.catalogInfo = CatalogInfo()
                            it.catalogInfo!!.productDisplayVersion = "display"
                            it.catalogInfo!!.productLineVersion = "product"
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
                            it.catalogInfo!!.productDisplayVersion = "display"
                            it.catalogInfo!!.productLineVersion = "product"
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
                            it.catalogInfo!!.productLineVersion = "product"
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
                            it.catalogInfo!!.productDisplayVersion = "display"
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
    fun shouldParse(visualStudioState: VisualStudioState, expectedInstance: VisualStudioInstance?) {
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