

package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.discovery.DefaultDiscoveredTargetNameFactory
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class DefaultDiscoveredTargetNameFactoryTest {
    @DataProvider
    fun testData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(DotnetCommandType.Build, "dir/cs.proj", "build dir/cs.proj"),
                arrayOf(DotnetCommandType.Build, "abc cs.proj", "build \"abc cs.proj\""),
                arrayOf(DotnetCommandType.Build, "cs.proj", "build cs.proj"),
                arrayOf(DotnetCommandType.NuGetPush, "dir/cs.proj", "nuget push dir/cs.proj"))
    }

    @Test(dataProvider = "testData")
    fun shouldCreateName(commandType: DotnetCommandType, path: String, expectedName: String) {
        // Given
        val nameFactory = DefaultDiscoveredTargetNameFactory()

        // When
        val actualName = nameFactory.createName(commandType, path)

        // Then
        Assert.assertEquals(actualName, expectedName)
    }
}