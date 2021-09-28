package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.every
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.dotnet.CommandTargetType
import jetbrains.buildServer.dotnet.TargetTypeProviderImpl
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class TargetTypeProviderTest {
    @DataProvider
    fun cases(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(File("Abc.dll"), CommandTargetType.Assembly),
                arrayOf(File("Abc.DLl"), CommandTargetType.Assembly),
                arrayOf(File(File("Path"), "Abc.DLl"), CommandTargetType.Assembly),

                arrayOf(File(File("Path"), "Abc.csproj"), CommandTargetType.Unknown),
                arrayOf(File("Abc.csproj"), CommandTargetType.Unknown),
                arrayOf(File(".csproj"), CommandTargetType.Unknown),
                arrayOf(File("Abc"), CommandTargetType.Unknown),
                arrayOf(File(""), CommandTargetType.Unknown)
        )
    }

    @Test(dataProvider = "cases")
    fun shouldProvideTargetType(file: File, expectedTargetType: CommandTargetType) {
        // Given
        val provider = createInstance()

        // When
        val actualTargetType = provider.getTargetType(file);

        // Then
        Assert.assertEquals(actualTargetType, expectedTargetType)
    }

    private fun createInstance() = TargetTypeProviderImpl()
}