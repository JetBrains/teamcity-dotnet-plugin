package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.discovery.*
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class ProjectTypeSelectorTest {
    @DataProvider
    fun testData(): Array<Array<Any>> {
        return arrayOf(
                // Test
                arrayOf(create(false,"Microsoft.NET.Test.Sdk"), setOf(ProjectType.Test)),
                arrayOf(create(false,"microsofT.net.test.SDK"), setOf(ProjectType.Test)),
                arrayOf(create(false,"Microsoft.NET.Test.Sdk", "abc"), setOf(ProjectType.Test)),
                arrayOf(create(false, "abc.Microsoft.NET.Test.Sdk"), setOf(ProjectType.Unknown)),
                arrayOf(create(false, "abcMicrosoft.NET.Test.Sdk"), setOf(ProjectType.Unknown)),
                arrayOf(create(false, "Microsoft.NET.Test.Sdk.abc"), setOf(ProjectType.Unknown)),
                arrayOf(create(false, "Microsoft.NET.Test.Sdkabc"), setOf(ProjectType.Unknown)),
                arrayOf(create(false, "Microsoft.NET.Test.Sdkþ"), setOf(ProjectType.Unknown)),
                arrayOf(create(false, "abc.Microsoft.NET.Test.Sdk.abc"), setOf(ProjectType.Unknown)),
                arrayOf(create(false, "abcMicrosoft.NET.Test.Sdkabc"), setOf(ProjectType.Unknown)),
                arrayOf(create(false, ".Microsoft.NET.Test."), setOf(ProjectType.Unknown)),
                // Publish
                arrayOf(create(false,"Microsoft.aspnet.Abc"), setOf(ProjectType.Publish)),
                arrayOf(create(false,"Microsoft.ASPNET.Abc"), setOf(ProjectType.Publish)),
                arrayOf(create(false,"Microsoft.aspnet.Abc", "abc"), setOf(ProjectType.Publish)),
                arrayOf(create(false,"Microsoft.aspnet."), setOf(ProjectType.Publish)),
                arrayOf(create(true,"Microsoft.aspnet.Abc"), setOf(ProjectType.Publish)),
                arrayOf(create(true), setOf(ProjectType.Publish)),
                arrayOf(create(false,"Microsoft.aspnet."), setOf(ProjectType.Publish)),
                arrayOf(create(false,".Microsoft.aspnet.abc"), setOf(ProjectType.Unknown)),
                arrayOf(create(false,"abc.Microsoft.aspnet.abc"), setOf(ProjectType.Unknown)),
                arrayOf(create(false,"abcMicrosoft.aspnetabc"), setOf(ProjectType.Unknown)),
                // Mixed
                arrayOf(create(true,"Microsoft.NET.Test.Sdk", "abc"), setOf(ProjectType.Publish, ProjectType.Test)),
                // Empty
                arrayOf(create(false, "abc"), setOf(ProjectType.Unknown)),
                arrayOf(create(), setOf(ProjectType.Unknown)))
    }

    @Test(dataProvider = "testData")
    fun shouldSelectProjectTypes(project: Project, expectedProjectTypes: Set<ProjectType>) {
        // Given
        val projectTypeSelector = ProjectTypeSelectorImpl()

        // When
        val actualProjectTypes = projectTypeSelector.select(project)

        // Then
        Assert.assertEquals(actualProjectTypes, expectedProjectTypes)
    }

    private fun create(generatePackageOnBuild: Boolean = false, vararg references: String): Project =
        Project("abc.proj", emptyList(), emptyList(), emptyList(), references.map { Reference(it) }, emptyList(), generatePackageOnBuild)
}