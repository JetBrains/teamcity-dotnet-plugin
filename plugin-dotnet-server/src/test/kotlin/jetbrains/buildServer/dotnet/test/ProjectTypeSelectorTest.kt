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
                arrayOf(create(false, "abc.Microsoft.NET.Test.Sdk"), emptySet<ProjectType>()),
                arrayOf(create(false, "abcMicrosoft.NET.Test.Sdk"), emptySet<ProjectType>()),
                arrayOf(create(false, "Microsoft.NET.Test.Sdk.abc"), emptySet<ProjectType>()),
                arrayOf(create(false, "Microsoft.NET.Test.Sdkabc"), emptySet<ProjectType>()),
                arrayOf(create(false, "Microsoft.NET.Test.Sdkþ"), emptySet<ProjectType>()),
                arrayOf(create(false, "abc.Microsoft.NET.Test.Sdk.abc"), emptySet<ProjectType>()),
                arrayOf(create(false, "abcMicrosoft.NET.Test.Sdkabc"), emptySet<ProjectType>()),
                arrayOf(create(false, ".Microsoft.NET.Test."), emptySet<ProjectType>()),
                // Publish
                arrayOf(create(false,"Microsoft.aspnet.Abc"), setOf(ProjectType.Publish)),
                arrayOf(create(false,"Microsoft.ASPNET.Abc"), setOf(ProjectType.Publish)),
                arrayOf(create(false,"Microsoft.aspnet.Abc", "abc"), setOf(ProjectType.Publish)),
                arrayOf(create(false,"Microsoft.aspnet."), setOf(ProjectType.Publish)),
                arrayOf(create(true,"Microsoft.aspnet.Abc"), setOf(ProjectType.Publish)),
                arrayOf(create(true), setOf(ProjectType.Publish)),
                arrayOf(create(false,"Microsoft.aspnet."), setOf(ProjectType.Publish)),
                arrayOf(create(false,".Microsoft.aspnet.abc"), emptySet<ProjectType>()),
                arrayOf(create(false,"abc.Microsoft.aspnet.abc"), emptySet<ProjectType>()),
                arrayOf(create(false,"abcMicrosoft.aspnetabc"), emptySet<ProjectType>()),
                // Mixed
                arrayOf(create(true,"Microsoft.NET.Test.Sdk", "abc"), setOf(ProjectType.Publish, ProjectType.Test)),
                // Empty
                arrayOf(create(false, "abc"), emptySet<ProjectType>()),
                arrayOf(create(), emptySet<ProjectType>()))
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