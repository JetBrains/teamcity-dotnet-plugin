package jetbrains.buildServer.inspect.test

import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_PREFIX_DOTNET_FAMEWORK
import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_SUFFIX_PATH
import jetbrains.buildServer.dotnet.Version
import jetbrains.buildServer.inspect.IspectionToolPlatform
import jetbrains.buildServer.inspect.RequirementsResolverImpl
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementQualifier
import jetbrains.buildServer.requirements.RequirementQualifier.EXISTS_QUALIFIER
import jetbrains.buildServer.requirements.RequirementType
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class RequirementsResolverTest {
    @DataProvider
    fun testData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(
                        Version(2018, 1),
                        IspectionToolPlatform.X64,
                        listOf(Requirement(EXISTS_QUALIFIER + "${CONFIG_PREFIX_DOTNET_FAMEWORK}[\\d\\.]+_x64${CONFIG_SUFFIX_PATH}", null, RequirementType.EXISTS))
                ),
                arrayOf(
                        Version(2018, 1),
                        IspectionToolPlatform.X86,
                        listOf(Requirement(EXISTS_QUALIFIER + "${CONFIG_PREFIX_DOTNET_FAMEWORK}[\\d\\.]+_x86${CONFIG_SUFFIX_PATH}", null, RequirementType.EXISTS))
                ),
                arrayOf(
                        Version(2018, 2),
                        IspectionToolPlatform.X64,
                        listOf(Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(${CONFIG_PREFIX_DOTNET_FAMEWORK}4\\.(6\\.(?!0)|[7-9]|[\\d]{2,})[\\d\\.]*_x64${CONFIG_SUFFIX_PATH})", null, RequirementType.EXISTS))
                ),
                arrayOf(
                        Version(2018, 2),
                        IspectionToolPlatform.X86,
                        listOf(Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(${CONFIG_PREFIX_DOTNET_FAMEWORK}4\\.(6\\.(?!0)|[7-9]|[\\d]{2,})[\\d\\.]*_x86${CONFIG_SUFFIX_PATH})", null, RequirementType.EXISTS))
                ),
                arrayOf(
                        Version(2018, 2),
                        IspectionToolPlatform.X86,
                        listOf(Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(${CONFIG_PREFIX_DOTNET_FAMEWORK}4\\.(6\\.(?!0)|[7-9]|[\\d]{2,})[\\d\\.]*_x86${CONFIG_SUFFIX_PATH})", null, RequirementType.EXISTS))
                )
        )
    }

    @Test(dataProvider = "testData")
    fun shouldResolve(version: Version, platform: IspectionToolPlatform, expectedRequierements: Collection<Requirement>) {
        // Given
        val resolver = RequirementsResolverImpl()

        // When
        val actualRequirements = resolver.resolve(version, platform).toList()

        // Then
        Assert.assertEquals(actualRequirements, expectedRequierements)
    }
}