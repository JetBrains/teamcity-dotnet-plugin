/*
 * Copyright 2000-2022 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.XmlDocumentServiceImpl
import jetbrains.buildServer.dotnet.discovery.*
import jetbrains.buildServer.dotnet.discovery.Target
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class MSBuildProjectDeserializerTest {
    @DataProvider
    fun testDeserializeData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(
                        "/Bank.Test.csproj",
                        Solution(listOf(Project("projectPath", listOf(Configuration("Debug"), Configuration("Release")), listOf(Framework("net452")), emptyList(), listOf(Reference("System"), Reference("Microsoft.VisualStudio.QualityTools.UnitTestFramework"), Reference("Microsoft.VisualStudio.QualityTools.CodedUITestFramework")), emptyList(), false, listOf(Property("AssemblyName", "Bank.Test"), Property("TestProjectType", "UnitTest"), Property("OutputType", "Library")))))),
                arrayOf(
                        "/project-runtime.csproj",
                        Solution(listOf(Project("projectPath", emptyList(), emptyList(), listOf(Runtime("win7-x64"), Runtime("win-7x86"), Runtime("ubuntu.16.10-x64")), emptyList(), emptyList(), false, listOf(Property("Sdk", "Microsoft.NET.Sdk")))))),
                arrayOf(
                        "/GeneratePackageOnBuild.csproj",
                        Solution(listOf(Project("projectPath", emptyList(), listOf(Framework("netstandard2.0")), emptyList(), emptyList(), emptyList(), true, listOf(Property("AssemblyName", "Nik.Nuget.Sample"), Property("Sdk", "Microsoft.NET.Sdk")))))),
                arrayOf(
                        "/project14.csproj",
                        Solution(listOf(Project("projectPath", listOf(Configuration("Debug"), Configuration("Release")), listOf(Framework("net20")), emptyList(), listOf(Reference("nunit.engine.api"), Reference("System"), Reference("System.Data"), Reference("System.Xml")), emptyList(), false, listOf(Property("AssemblyName", "teamcity-event-listener"), Property("OutputType", "Library")))))),
                arrayOf(
                        "/project.csproj",
                        Solution(listOf(Project("projectPath", listOf(Configuration("Core")), listOf(Framework("netcoreapp1.0")), emptyList(), listOf(Reference("Microsoft.NET.Sdk"), Reference("Microsoft.NET.Test.Sdk")), emptyList(), false, listOf(Property("OutputType", "Exe")))))),
                arrayOf(
                        "/build.proj",
                        Solution(listOf(Project("projectPath", listOf(Configuration("Release")), emptyList(), emptyList(), emptyList(), listOf(Target("GetNuGet"), Target("Build"), Target("Test")), false, emptyList())))),
                arrayOf(
                        "/project-simplified.csproj",
                        Solution(listOf(Project("projectPath", listOf(Configuration("Core")), listOf(Framework("netcoreapp1.0")), emptyList(), listOf(Reference("Microsoft.NET.Sdk"), Reference("Microsoft.NET.Test.Sdk")), emptyList(), false, listOf(Property("OutputType", "Exe"), Property("Sdk", "Microsoft.NET.Sdk")))))),
                arrayOf(
                        "/project-frameworks.csproj",
                        Solution(listOf(Project("projectPath", emptyList(), listOf(Framework("net45"), Framework("netstandard1.3")), emptyList(), listOf(Reference("Newtonsoft.Json")), emptyList(), false, listOf(Property("Sdk", "Microsoft.NET.Sdk")))))))
    }

    @Test(dataProvider = "testDeserializeData")
    fun shouldDeserialize(target: String, expectedSolution: Solution) {
        // Given
        val path = "projectPath"
        val streamFactory = StreamFactoryStub().add(path, this::class.java.getResourceAsStream(target))
        val deserializer = MSBuildProjectDeserializer(XmlDocumentServiceImpl())

        // When
        val actualSolution = deserializer.deserialize(path, streamFactory)

        // Then
        Assert.assertEquals(actualSolution, expectedSolution)
    }

    @DataProvider
    fun testAcceptData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf("abc.proj", true),
                arrayOf("abcPproj", false),
                arrayOf("abc.csproj", true),
                arrayOf("abc.vbproj", true),
                arrayOf("abc3232.vbproj", true),
                arrayOf("abc.Proj", true),
                arrayOf("abc.CSproj", true),
                arrayOf("abc.VBproj", true),
                arrayOf("ab c.VBproj", true),
                arrayOf("dd/ff/abc.VBproj", true),
                arrayOf("c:\\dd\\ff\\abc.VBproj", true),
                arrayOf("abc.sln", false),
                arrayOf("abc.", false),
                arrayOf("abc", false),
                arrayOf("abc.projddd", false),
                arrayOf(".proj", false),
                arrayOf("proj", false),
                arrayOf("csproj", false),
                arrayOf("VBproj", false),
                arrayOf("   ", false),
                arrayOf("", false))
    }

    @Test(dataProvider = "testAcceptData")
    fun shouldAccept(path: String, expectedAccepted: Boolean) {
        // Given
        val deserializer = MSBuildProjectDeserializer(XmlDocumentServiceImpl())

        // When
        val actualAccepted = deserializer.isAccepted(path)

        // Then
        Assert.assertEquals(actualAccepted, expectedAccepted)
    }
}