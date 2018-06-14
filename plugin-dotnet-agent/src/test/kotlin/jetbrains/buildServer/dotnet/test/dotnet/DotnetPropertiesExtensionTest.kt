package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.dotnet.*
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class DotnetPropertiesExtensionTest {
    @DataProvider
    fun testData(): Array<Array<Sequence<DotnetPropertiesExtension.Sdk>>>{
        return arrayOf(
                arrayOf(
                        emptySequence<DotnetPropertiesExtension.Sdk>(),
                        emptySequence<DotnetPropertiesExtension.Sdk>()),

                arrayOf(
                        sequenceOf(
                                DotnetPropertiesExtension.Sdk(File("1.0.0"), Version(1, 0, 0))),
                        sequenceOf(
                                DotnetPropertiesExtension.Sdk(File("1.0.0"), Version(1, 0)),
                                DotnetPropertiesExtension.Sdk(File("1.0.0"), Version(1, 0, 0)))),

                // Select newest version as default for group by Version(x, y)
                arrayOf(
                        sequenceOf(
                                DotnetPropertiesExtension.Sdk(File("1.1.100"), Version(1, 1, 100)),
                                DotnetPropertiesExtension.Sdk(File("1.1.300"), Version(1, 1, 300)),
                                DotnetPropertiesExtension.Sdk(File("1.1.1"), Version(1, 1, 1))),
                        sequenceOf(
                                DotnetPropertiesExtension.Sdk(File("1.1.300"), Version(1, 1)),
                                DotnetPropertiesExtension.Sdk(File("1.1.100"), Version(1, 1, 100)),
                                DotnetPropertiesExtension.Sdk(File("1.1.300"), Version(1, 1, 300)),
                                DotnetPropertiesExtension.Sdk(File("1.1.1"), Version(1, 1, 1))))
        )
    }

    @Test(dataProvider = "testData")
    fun shouldEnumerateSdkConfigurationParameters(
            originSdks: Sequence<DotnetPropertiesExtension.Sdk>,
            expectedSdks: Sequence<DotnetPropertiesExtension.Sdk>) {
        // Given

        // When
        val actualSdks = DotnetPropertiesExtension.enumerateSdk(originSdks).toList();

        // Then
        Assert.assertEquals(actualSdks, expectedSdks.toList())
    }
}