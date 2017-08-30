package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotcover.CoverageFilter
import jetbrains.buildServer.dotcover.CoverageFilterProvider
import jetbrains.buildServer.dotcover.CoverageFilterProviderImpl
import jetbrains.buildServer.dotnet.DotCoverConstants
import jetbrains.buildServer.runners.Converter
import jetbrains.buildServer.runners.ParameterType
import jetbrains.buildServer.runners.ParametersService
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.util.*

@Suppress("UNCHECKED_CAST")
class CoverageFilterProviderTest {
    private var _ctx: Mockery? = null
    private var _coverageFilterConverter: Converter<String, Sequence<CoverageFilter>>? = null
    private var _parametersService: ParametersService? = null

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _coverageFilterConverter = _ctx!!.mock(Converter::class.java) as Converter<String, Sequence<CoverageFilter>>
        _parametersService = _ctx!!.mock(ParametersService::class.java)
    }

    @Test
    fun shouldGetFiltersWhenHasFilters() {
        // Given
        val instance = createInstance()

        // When
        _ctx!!.checking(object : Expectations() {
            init {
                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, DotCoverConstants.PARAM_FILTERS)
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
            listOf(
                CoverageFilter(CoverageFilter.CoverageFilterType.Include, CoverageFilter.Any, "aaa", CoverageFilter.Any, CoverageFilter.Any),
                CoverageFilter(CoverageFilter.CoverageFilterType.Exclude, CoverageFilter.Any, "bbb", CoverageFilter.Any, CoverageFilter.Any)))
    }

    @Test
    fun shouldGetFiltersWhenHasNoFilters() {
        // Given
        val instance = createInstance()

        // When
        _ctx!!.checking(object : Expectations() {
            init {
                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, DotCoverConstants.PARAM_FILTERS)
                will(returnValue("some filter"))

                oneOf<Converter<String, Sequence<CoverageFilter>>>(_coverageFilterConverter).convert("some filter")
                will(returnValue(emptySequence<CoverageFilter>()))
            }
        })

        val filters = instance.filters.toList()

        // Then
        Assert.assertEquals(
            filters,
            listOf(CoverageFilter(CoverageFilter.CoverageFilterType.Include, CoverageFilter.Any, CoverageFilter.Any, CoverageFilter.Any, CoverageFilter.Any)))
    }

    @Test
    fun shouldAddDefaultWhenHasOutdatedAnyFilterForInclude() {
        // Given
        val instance = createInstance()

        // When
        _ctx!!.checking(object : Expectations() {
            init {
                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, DotCoverConstants.PARAM_FILTERS)
                will(returnValue("some filter"))

                oneOf<Converter<String, Sequence<CoverageFilter>>>(_coverageFilterConverter).convert("some filter")
                will(returnValue(sequenceOf(
                    CoverageFilter(CoverageFilter.CoverageFilterType.Include, CoverageFilter.Any, "*.*", CoverageFilter.Any, CoverageFilter.Any)
                )))
            }
        })

        val filters = instance.filters.toList()

        // Then
        Assert.assertEquals(
            filters,
            listOf(
                CoverageFilter(CoverageFilter.CoverageFilterType.Include, CoverageFilter.Any, CoverageFilter.Any, CoverageFilter.Any, CoverageFilter.Any),
                CoverageFilter(CoverageFilter.CoverageFilterType.Include, CoverageFilter.Any, "*.*", CoverageFilter.Any, CoverageFilter.Any)))
    }

    @Test
    fun shouldAddDefaultWhenHasOutdatedAnyFilterForExclude() {
        // Given
        val instance = createInstance()

        // When
        _ctx!!.checking(object : Expectations() {
            init {
                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, DotCoverConstants.PARAM_FILTERS)
                will(returnValue("some filter"))

                oneOf<Converter<String, Sequence<CoverageFilter>>>(_coverageFilterConverter).convert("some filter")
                will(returnValue(sequenceOf(
                    CoverageFilter(CoverageFilter.CoverageFilterType.Exclude, CoverageFilter.Any, "*.*", CoverageFilter.Any, CoverageFilter.Any)
                )))
            }
        })

        val filters = instance.filters.toList()

        // Then
        Assert.assertEquals(
            filters,
            listOf(
                CoverageFilter(CoverageFilter.CoverageFilterType.Exclude, CoverageFilter.Any, CoverageFilter.Any, CoverageFilter.Any, CoverageFilter.Any),
                CoverageFilter(CoverageFilter.CoverageFilterType.Exclude, CoverageFilter.Any, "*.*", CoverageFilter.Any, CoverageFilter.Any)))
    }

    @Test
    fun shouldGetAttributeFiltersWhenHasFilters() {
        // Given
        val instance = createInstance()

        // When
        _ctx!!.checking(object : Expectations() {
            init {
                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, DotCoverConstants.PARAM_ATTRIBUTE_FILTERS)
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
        Assert.assertEquals(
            filters,
            listOf(
                CoverageFilter(CoverageFilter.CoverageFilterType.Exclude, CoverageFilter.Any, CoverageFilter.Any, "bbb", CoverageFilter.Any),
                CoverageFilter(CoverageFilter.CoverageFilterType.Exclude, CoverageFilter.Any, CoverageFilter.Any, "zzz", CoverageFilter.Any),
                CoverageFilter(CoverageFilter.CoverageFilterType.Exclude, CoverageFilter.Any, CoverageFilter.Any, "System.Diagnostics.CodeAnalysis.ExcludeFromCodeCoverageAttribute", CoverageFilter.Any)))
    }

    @Test
    fun shouldGetAttributeFiltersWhenHasNoFilters() {
        // Given
        val instance = createInstance()

        // When
        _ctx!!.checking(object : Expectations() {
            init {
                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, DotCoverConstants.PARAM_ATTRIBUTE_FILTERS)
                will(returnValue(null))
            }
        })

        val filters = instance.attributeFilters.toList()

        // Then
        Assert.assertEquals(
            filters,
            listOf(CoverageFilter(CoverageFilter.CoverageFilterType.Exclude, CoverageFilter.Any, CoverageFilter.Any, "System.Diagnostics.CodeAnalysis.ExcludeFromCodeCoverageAttribute", CoverageFilter.Any)))
    }

    private fun createInstance(): CoverageFilterProvider {
        return CoverageFilterProviderImpl(_parametersService!!, _coverageFilterConverter!!)
    }
}