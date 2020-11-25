package jetbrains.buildServer.dotnet.test.agent

import jetbrains.buildServer.JsonParserImpl
import jetbrains.buildServer.visualStudio.JsonVisualStudioInstanceParser
import org.testng.Assert
import org.testng.annotations.Test
import java.io.*

class JsonParserTest {
    @Test
    fun shouldParse() {
        // Given
        var stateFile = File(JsonParserTest::class.java.classLoader.getResource("vs2019_state.json")!!.file)

        // When
        FileInputStream(stateFile).use {
            BufferedReader(InputStreamReader(it)).use {
                val state = JsonParserImpl().tryParse<JsonVisualStudioInstanceParser.VisualStudioState>(it, JsonVisualStudioInstanceParser.VisualStudioState::class.java)

                // Then
                Assert.assertNotNull(state)
                Assert.assertEquals(state?.installationPath, "C:\\Program Files (x86)\\Microsoft Visual Studio\\2019\\Professional")
                Assert.assertEquals(state?.catalogInfo?.productDisplayVersion, "16.5.4")
                Assert.assertEquals(state?.catalogInfo?.productLineVersion, "2019")
            }
        }
    }
}