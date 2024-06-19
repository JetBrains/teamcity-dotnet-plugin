package jetbrains.buildServer.nunit

import jetbrains.buildServer.tools.available.DownloadableToolVersion
import org.testng.Assert.assertEquals
import org.testng.annotations.Test
import java.io.StringReader

class ToolsParserTest {
    @Test
    fun `should parse github json`() {
        // arrange
        val parser = ToolsParserImpl()

        // act
        val tools = parser.parse(StringReader(GithubResponse))

        // assert
        assertEquals(tools.size, 2)
        assertEquals(tools[0].destinationFileName, "NUnit.Console-3.6.1.zip")
        assertEquals(tools[1].destinationFileName, "NUnit.Console-3.6.zip")
    }

    @Test
    fun `should return empty list in case of error`() {
        // arrange
        val parser = ToolsParserImpl()

        // act
        val tools = parser.parse(StringReader("wrong-json"))

        // assert
        assertEquals(tools, emptyList<DownloadableToolVersion>())
    }

    companion object {
        private val GithubResponse = """
            [
              {
                "url": "https://api.github.com/repos/nunit/nunit/releases/5571889",
                "assets": [
                  {
                    "name": "NUnit.3.6.1.nupkg",
                    "browser_download_url": "https://github.com/nunit/nunit/releases/download/3.6.1/NUnit.3.6.1.nupkg"
                  },
                  {
                    "name": "NUnit.Console-3.6.1.zip",
                    "browser_download_url": "https://github.com/nunit/nunit/releases/download/3.6.1/NUnit.Console-3.6.1.zip"
                  },
                  {
                    "name": "NUnitLite.3.6.1.nupkg",
                    "browser_download_url": "https://github.com/nunit/nunit/releases/download/3.6.1/NUnitLite.3.6.1.nupkg"
                  }
                ]
              },
              {
            	"url": "https://api.github.com/repos/nunit/nunit/releases/5571889",
                "assets": [
                  {
                    "name": "NUnit.3.6.1.nupkg",
                    "browser_download_url": "https://github.com/nunit/nunit/releases/download/3.6.1/NUnit.3.6.1.nupkg"
                  },
                  {
                    "name": "NUnit.Console-3.6.zip",
                    "browser_download_url": "https://github.com/nunit/nunit/releases/download/3.6.1/NUnit.Console-3.6.zip"
                  },
                  {
                    "name": "NUnitLite.3.6.1.nupkg",
                    "browser_download_url": "https://github.com/nunit/nunit/releases/download/3.6.1/NUnitLite.3.6.1.nupkg"
                  }
                ]
              }
            ]
        """.trimIndent()
    }
}