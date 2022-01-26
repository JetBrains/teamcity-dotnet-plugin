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

package jetbrains.buildServer.dotnet.test.dotcover

import jetbrains.buildServer.agent.runner.Converter
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotcover.CoverageFilter
import jetbrains.buildServer.dotcover.CoverageFilterProvider
import jetbrains.buildServer.dotcover.CoverageFilterProviderImpl
import jetbrains.buildServer.dotcover.DotCoverFilterConverter
import jetbrains.buildServer.dotnet.CoverageConstants
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class CoverageFilterProviderTest {
    private lateinit var _ctx: Mockery
    private lateinit var _coverageFilterConverter: DotCoverFilterConverter
    private lateinit var _parametersService: ParametersService

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _coverageFilterConverter = _ctx.mock(DotCoverFilterConverter::class.java)
        _parametersService = _ctx.mock(ParametersService::class.java)
    }

    @Test
    fun shouldGetFiltersWhenHasFilters() {
        // Given
        val instance = createInstance()

        // When
        _ctx.checking(object : Expectations() {
            init {
                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_FILTERS)
                will(returnValue("some filter"))

                oneOf<Converter<String, Sequence<CoverageFilter>>>(_coverageFilterConverter).convert("some filter")
                will(returnValue(sequenceOf(
                        CoverageFilter(CoverageFilter.CoverageFilterType.Include, CoverageFilter.Any, "aaa", CoverageFilter.Any, CoverageFilter.Any),
                        CoverageFilter(CoverageFilter.CoverageFilterType.Exclude, CoverageFilter.Any, "bbb", CoverageFilter.Any, CoverageFilter.Any)
                )))
            }
        })

        val filters = instance.filters.toList()

        // Then
        Assert.assertEquals(
                filters,
                sequenceOf(
                        CoverageFilter(CoverageFilter.CoverageFilterType.Include, CoverageFilter.Any, "aaa", CoverageFilter.Any, CoverageFilter.Any),
                        CoverageFilter(CoverageFilter.CoverageFilterType.Exclude, CoverageFilter.Any, "bbb", CoverageFilter.Any, CoverageFilter.Any))
                        .plus(CoverageFilterProviderImpl.DefaultExcludeFilters).toList())
    }

    @Test
    fun shouldGetFiltersWhenHasNoFilters() {
        // Given
        val instance = createInstance()

        // When
        _ctx.checking(object : Expectations() {
            init {
                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_FILTERS)
                will(returnValue("some filter"))

                oneOf<Converter<String, Sequence<CoverageFilter>>>(_coverageFilterConverter).convert("some filter")
                will(returnValue(emptySequence<CoverageFilter>()))
            }
        })

        val filters = instance.filters.toList()

        // Then
        _ctx.assertIsSatisfied()
        Assert.assertEquals(
                filters,
                sequenceOf(CoverageFilter(CoverageFilter.CoverageFilterType.Include, CoverageFilter.Any, CoverageFilter.Any, CoverageFilter.Any, CoverageFilter.Any))
                        .plus(CoverageFilterProviderImpl.DefaultExcludeFilters).toList())
    }

    @Test
    fun shouldAddDefaultWhenHasOutdatedAnyFilterForInclude() {
        // Given
        val instance = createInstance()

        // When
        _ctx.checking(object : Expectations() {
            init {
                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_FILTERS)
                will(returnValue("some filter"))

                oneOf<Converter<String, Sequence<CoverageFilter>>>(_coverageFilterConverter).convert("some filter")
                will(returnValue(sequenceOf(
                        CoverageFilter(CoverageFilter.CoverageFilterType.Include, CoverageFilter.Any, "*.*", CoverageFilter.Any, CoverageFilter.Any)
                )))
            }
        })

        val filters = instance.filters.toList()

        // Then
        _ctx.assertIsSatisfied()
        Assert.assertEquals(
                filters,
                sequenceOf(
                        CoverageFilter(CoverageFilter.CoverageFilterType.Include, CoverageFilter.Any, CoverageFilter.Any, CoverageFilter.Any, CoverageFilter.Any),
                        CoverageFilter(CoverageFilter.CoverageFilterType.Include, CoverageFilter.Any, "*.*", CoverageFilter.Any, CoverageFilter.Any))
                        .plus(CoverageFilterProviderImpl.DefaultExcludeFilters).toList())
    }

    @Test
    fun shouldAddDefaultWhenHasOutdatedAnyFilterForExclude() {
        // Given
        val instance = createInstance()

        // When
        _ctx.checking(object : Expectations() {
            init {
                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_FILTERS)
                will(returnValue("some filter"))

                oneOf<Converter<String, Sequence<CoverageFilter>>>(_coverageFilterConverter).convert("some filter")
                will(returnValue(sequenceOf(
                        CoverageFilter(CoverageFilter.CoverageFilterType.Exclude, CoverageFilter.Any, "*.*", CoverageFilter.Any, CoverageFilter.Any)
                )))
            }
        })

        val filters = instance.filters.toList()

        // Then
        _ctx.assertIsSatisfied()
        Assert.assertEquals(
                filters,
                sequenceOf(
                        CoverageFilter(CoverageFilter.CoverageFilterType.Exclude, CoverageFilter.Any, CoverageFilter.Any, CoverageFilter.Any, CoverageFilter.Any),
                        CoverageFilter(CoverageFilter.CoverageFilterType.Exclude, CoverageFilter.Any, "*.*", CoverageFilter.Any, CoverageFilter.Any))
                        .plus(CoverageFilterProviderImpl.DefaultExcludeFilters).toList())
    }

    @Test
    fun shouldGetAttributeFiltersWhenHasFilters() {
        // Given
        val instance = createInstance()

        // When
        _ctx.checking(object : Expectations() {
            init {
                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_ATTRIBUTE_FILTERS)
                will(returnValue("some filter"))

                oneOf<Converter<String, Sequence<CoverageFilter>>>(_coverageFilterConverter).convert("some filter")
                will(returnValue(sequenceOf(
                        CoverageFilter(CoverageFilter.CoverageFilterType.Include, CoverageFilter.Any, CoverageFilter.Any, "aaa", CoverageFilter.Any),
                        CoverageFilter(CoverageFilter.CoverageFilterType.Exclude, CoverageFilter.Any, CoverageFilter.Any, "bbb", CoverageFilter.Any),
                        CoverageFilter(CoverageFilter.CoverageFilterType.Exclude, "zzz", CoverageFilter.Any, CoverageFilter.Any, CoverageFilter.Any),
                        CoverageFilter(CoverageFilter.CoverageFilterType.Exclude, CoverageFilter.Any, "ccc", CoverageFilter.Any, CoverageFilter.Any),
                        CoverageFilter(CoverageFilter.CoverageFilterType.Exclude, CoverageFilter.Any, CoverageFilter.Any, CoverageFilter.Any, "ddd")
                )))
            }
        })

        val filters = instance.attributeFilters.toList()

        // Then
        _ctx.assertIsSatisfied()
        Assert.assertEquals(
                filters,
                sequenceOf(
                        CoverageFilter(CoverageFilter.CoverageFilterType.Exclude, CoverageFilter.Any, CoverageFilter.Any, "bbb", CoverageFilter.Any),
                        CoverageFilter(CoverageFilter.CoverageFilterType.Exclude, CoverageFilter.Any, CoverageFilter.Any, "zzz", CoverageFilter.Any))
                        .plus(CoverageFilterProviderImpl.DefaultExcludeAttributeFilters)
                        .toList())
    }

    @Test
    fun shouldGetAttributeFiltersWhenHasNoFilters() {
        // Given
        val instance = createInstance()

        // When
        _ctx.checking(object : Expectations() {
            init {
                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_ATTRIBUTE_FILTERS)
                will(returnValue(null))
            }
        })

        val filters = instance.attributeFilters.toList()

        // Then
        _ctx.assertIsSatisfied()
        Assert.assertEquals(filters, CoverageFilterProviderImpl.DefaultExcludeAttributeFilters.toList())
    }

    private fun createInstance(): CoverageFilterProvider {
        return CoverageFilterProviderImpl(_parametersService, _coverageFilterConverter)
    }
}