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

import jetbrains.buildServer.dotnet.discovery.*
import jetbrains.buildServer.dotnet.discovery.Target
import jetbrains.buildServer.dotnet.fetchers.DotnetTargetsFetcher
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class DotnetTargetsFetcherTest {
    @DataProvider
    fun testData(): Array<Array<Any>> {
        return arrayOf(
                // Test
                arrayOf(
                        create(),
                        setOf(ProjectType.Test),
                        DotnetTargetsFetcher.InitialDefaultTargets.plus(DotnetTargetsFetcher.TestTargets).plus(DotnetTargetsFetcher.FinishDefaultTargets)),
                // Publish
                arrayOf(
                        create(),
                        setOf(ProjectType.Test),
                        DotnetTargetsFetcher.InitialDefaultTargets.plus(DotnetTargetsFetcher.TestTargets).plus(DotnetTargetsFetcher.FinishDefaultTargets)),
                // Mixed
                arrayOf(
                        create(),
                        setOf(ProjectType.Publish, ProjectType.Test),
                        DotnetTargetsFetcher.InitialDefaultTargets.plus(DotnetTargetsFetcher.TestTargets).plus(DotnetTargetsFetcher.PublishTargets).plus(DotnetTargetsFetcher.FinishDefaultTargets)),
                // Custom
                arrayOf(
                        create("a bc"),
                        setOf(ProjectType.Publish, ProjectType.Test),
                        DotnetTargetsFetcher.InitialDefaultTargets.plus("\"a bc\"").plus(DotnetTargetsFetcher.TestTargets).plus(DotnetTargetsFetcher.PublishTargets).plus(DotnetTargetsFetcher.FinishDefaultTargets)),
                arrayOf(
                        create("abc"),
                        setOf(ProjectType.Publish, ProjectType.Test),
                        DotnetTargetsFetcher.InitialDefaultTargets.plus("abc").plus(DotnetTargetsFetcher.TestTargets).plus(DotnetTargetsFetcher.PublishTargets).plus(DotnetTargetsFetcher.FinishDefaultTargets)),
                arrayOf(
                        create("xyz", "Abc"),
                        setOf(ProjectType.Publish, ProjectType.Test),
                        DotnetTargetsFetcher.InitialDefaultTargets.plus("xyz").plus("Abc").plus(DotnetTargetsFetcher.TestTargets).plus(DotnetTargetsFetcher.PublishTargets).plus(DotnetTargetsFetcher.FinishDefaultTargets)),
                arrayOf(
                        create("xyz", ""),
                        setOf(ProjectType.Publish, ProjectType.Test),
                        DotnetTargetsFetcher.InitialDefaultTargets.plus("xyz").plus(DotnetTargetsFetcher.TestTargets).plus(DotnetTargetsFetcher.PublishTargets).plus(DotnetTargetsFetcher.FinishDefaultTargets)),
                arrayOf(
                        create("xyz", "  "),
                        setOf(ProjectType.Publish, ProjectType.Test),
                        DotnetTargetsFetcher.InitialDefaultTargets.plus("xyz").plus(DotnetTargetsFetcher.TestTargets).plus(DotnetTargetsFetcher.PublishTargets).plus(DotnetTargetsFetcher.FinishDefaultTargets)),
                arrayOf(
                        create("abc", "xyz", "abc"),
                        setOf(ProjectType.Publish, ProjectType.Test),
                        DotnetTargetsFetcher.InitialDefaultTargets.plus("abc").plus("xyz").plus(DotnetTargetsFetcher.TestTargets).plus(DotnetTargetsFetcher.PublishTargets).plus(DotnetTargetsFetcher.FinishDefaultTargets)),
                arrayOf(
                        create("abc", "xyz", "ABc"),
                        setOf(ProjectType.Publish, ProjectType.Test),
                        DotnetTargetsFetcher.InitialDefaultTargets.plus("abc").plus("xyz").plus(DotnetTargetsFetcher.TestTargets).plus(DotnetTargetsFetcher.PublishTargets).plus(DotnetTargetsFetcher.FinishDefaultTargets)),
                arrayOf(
                        create("abc", "xyz", DotnetTargetsFetcher.InitialDefaultTargets.first()),
                        setOf(ProjectType.Publish, ProjectType.Test),
                        DotnetTargetsFetcher.InitialDefaultTargets.plus("abc").plus("xyz").plus(DotnetTargetsFetcher.TestTargets).plus(DotnetTargetsFetcher.PublishTargets).plus(DotnetTargetsFetcher.FinishDefaultTargets)),
                arrayOf(
                        create(DotnetTargetsFetcher.InitialDefaultTargets.first()),
                        setOf(ProjectType.Publish, ProjectType.Test),
                        DotnetTargetsFetcher.InitialDefaultTargets.plus(DotnetTargetsFetcher.TestTargets).plus(DotnetTargetsFetcher.PublishTargets).plus(DotnetTargetsFetcher.FinishDefaultTargets)),
                arrayOf(
                        create("abc", "xyz", DotnetTargetsFetcher.TestTargets.first()),
                        setOf(ProjectType.Publish, ProjectType.Test),
                        DotnetTargetsFetcher.InitialDefaultTargets.plus("abc").plus("xyz").plus(DotnetTargetsFetcher.TestTargets).plus(DotnetTargetsFetcher.PublishTargets).plus(DotnetTargetsFetcher.FinishDefaultTargets)),
                arrayOf(
                        create(DotnetTargetsFetcher.PublishTargets.first(), "abc", DotnetTargetsFetcher.TestTargets.first(), "xyz"),
                        setOf(ProjectType.Publish, ProjectType.Test),
                        DotnetTargetsFetcher.InitialDefaultTargets.plus("abc").plus("xyz").plus(DotnetTargetsFetcher.TestTargets).plus(DotnetTargetsFetcher.PublishTargets).plus(DotnetTargetsFetcher.FinishDefaultTargets)),
                arrayOf(
                        create(DotnetTargetsFetcher.PublishTargets.first().toUpperCase(), "abc", DotnetTargetsFetcher.TestTargets.first().toUpperCase(), "xyz"),
                        setOf(ProjectType.Publish, ProjectType.Test),
                        DotnetTargetsFetcher.InitialDefaultTargets.plus("abc").plus("xyz").plus(DotnetTargetsFetcher.TestTargets).plus(DotnetTargetsFetcher.PublishTargets).plus(DotnetTargetsFetcher.FinishDefaultTargets)),
                // Default
                arrayOf(
                        create(),
                        setOf<ProjectType>(),
                        DotnetTargetsFetcher.InitialDefaultTargets.plus(DotnetTargetsFetcher.FinishDefaultTargets)))
    }

    @Test(dataProvider = "testData")
    fun shouldFetchTargets(
            project: Project,
            projectTypes: Set<ProjectType>,
            expectedTargets: List<String>) {
        // Given
        val paths = sequenceOf("dir1/proj1.csproj", "dir2/proj2.json")
        val ctx = Mockery()
        val solutionDiscover = ctx.mock(SolutionDiscover::class.java)
        val streamFactory = ctx.mock(StreamFactory::class.java)
        val projectTypeSelector = ctx.mock(ProjectTypeSelector::class.java)

        ctx.checking(object : Expectations() {
            init {
                allowing<ProjectTypeSelector>(projectTypeSelector).select(project)
                will(returnValue(projectTypes))

                oneOf<SolutionDiscover>(solutionDiscover).discover(streamFactory, paths)
                will(returnValue(listOf(Solution(listOf(project)))))
            }
        })

        val fetcher = DotnetTargetsFetcher(solutionDiscover, projectTypeSelector)

        // When
        val actualTargets = fetcher.getValues(streamFactory, paths).toList()

        // Then
        Assert.assertEquals(actualTargets, expectedTargets.toList())
    }

    private fun create(vararg targets: String): Project =
            Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), emptyList(), targets.map { Target(it) })
}