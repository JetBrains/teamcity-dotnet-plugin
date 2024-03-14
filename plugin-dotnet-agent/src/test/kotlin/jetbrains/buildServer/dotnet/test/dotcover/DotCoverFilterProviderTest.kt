package jetbrains.buildServer.dotnet.test.dotcover

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotcover.CoverageFilter
import jetbrains.buildServer.dotcover.DotCoverFilterProvider
import jetbrains.buildServer.dotcover.DotCoverFilterConverter
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.DotnetConstants
import org.testng.Assert.assertEquals
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class DotCoverFilterProviderTest {
    @MockK private lateinit var _parametersServiceMock: ParametersService
    @MockK private lateinit var _coverageFilterConverterMock: DotCoverFilterConverter
    private lateinit var _instance: DotCoverFilterProvider

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        clearAllMocks()
        _instance = DotCoverFilterProvider(_parametersServiceMock, _coverageFilterConverterMock)
    }

    @Test
    fun `should provide only filters from the parameter when default filters are disabled`() {
        // arrange
        val paramFiltersMock = sequenceOf<CoverageFilter>(
            CoverageFilter(
                type = CoverageFilter.CoverageFilterType.Include,
                defaultMask = CoverageFilter.Any,
                moduleMask = "aaa",
                classMask = CoverageFilter.Any,
                functionMask = CoverageFilter.Any
            ),
            CoverageFilter(
                type = CoverageFilter.CoverageFilterType.Include,
                defaultMask = CoverageFilter.Any,
                moduleMask = "bbb",
                classMask = CoverageFilter.Any,
                functionMask = CoverageFilter.Any
            )
        )
        _parametersServiceMock.also {
            every { it.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_FILTERS) } returns "SOME_FILTER_VALUE"
            every { it.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_DOTCOVER_WRAPPER_COVER_DEFAULT_ASSEMBLY_FILTERS_ENABLED) } returns "false"
        }
        every { _coverageFilterConverterMock.convert(any()) } returns paramFiltersMock

        // act
        val result = _instance.filters.toList()

        // assert
        assertEquals(result, paramFiltersMock.toList())
    }

    @Test
    fun `should provide both default and filters from the parameter when default filters are enabled`() {
        // arrange
        val paramFiltersMock = sequenceOf<CoverageFilter>(
            CoverageFilter(
                type = CoverageFilter.CoverageFilterType.Include,
                defaultMask = CoverageFilter.Any,
                moduleMask = "aaa",
                classMask = CoverageFilter.Any,
                functionMask = CoverageFilter.Any
            ),
            CoverageFilter(
                type = CoverageFilter.CoverageFilterType.Include,
                defaultMask = CoverageFilter.Any,
                moduleMask = "bbb",
                classMask = CoverageFilter.Any,
                functionMask = CoverageFilter.Any
            )
        )
        _parametersServiceMock.also {
            every { it.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_FILTERS) } returns "SOME_FILTER_VALUE"
        }
        every { _coverageFilterConverterMock.convert(any()) } returns paramFiltersMock

        // act
        val result = _instance.filters.toList()

        // assert
        assertEquals(result, paramFiltersMock.plus(DotCoverFilterProvider.DefaultExcludeFilters).toList())
    }

    @Test
    fun `should provide default exclude and include filters when default filters are enabled and no filters from the parameter`() {
        // arrange
        _parametersServiceMock.also {
            every { it.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_FILTERS) } returns "SOME_FILTER_VALUE"
        }
        every { _coverageFilterConverterMock.convert(any()) } returns emptySequence()

        // act
        val result = _instance.filters.toList()

        // assert
        assertEquals(result, DotCoverFilterProvider.DefaultIncludeFilters.plus(DotCoverFilterProvider.DefaultExcludeFilters).toList())
    }

    @DataProvider
    fun `filter types`() = arrayOf(
        arrayOf(CoverageFilter.CoverageFilterType.Include),
        arrayOf(CoverageFilter.CoverageFilterType.Exclude),
    )
    @Test(dataProvider = "filter types")
    fun `should provide additional filters when has outdated any filter for include`(filterType: CoverageFilter.CoverageFilterType) {
        // arrange
        val paramFiltersMock = sequenceOf<CoverageFilter>(
            CoverageFilter(
                type = filterType,
                defaultMask = CoverageFilter.Any,
                moduleMask = "*.*",
                classMask = CoverageFilter.Any,
                functionMask = CoverageFilter.Any
            ),
        )
        _parametersServiceMock.also {
            every { it.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_FILTERS) } returns "SOME_FILTER_VALUE"
        }
        every { _coverageFilterConverterMock.convert(any()) } returns paramFiltersMock

        // act
        val result = _instance.filters.toList()

        // assert
        assertEquals(
            result,
            sequenceOf(
                CoverageFilter(
                    type = filterType,
                    defaultMask = CoverageFilter.Any,
                    moduleMask = CoverageFilter.Any,
                    classMask = CoverageFilter.Any,
                    functionMask = CoverageFilter.Any
                )
            )
                .plus(paramFiltersMock)
                .plus(DotCoverFilterProvider.DefaultExcludeFilters).toList()
        )
    }

    @Test
    fun `should provide attribute filters both default and from the param when default filters are enabled`() {
        // arrange
        val paramFiltersMock = sequenceOf<CoverageFilter>(
            CoverageFilter(
                type = CoverageFilter.CoverageFilterType.Include,
                defaultMask = CoverageFilter.Any,
                moduleMask = CoverageFilter.Any,
                classMask = "aaa",
                functionMask = CoverageFilter.Any
            ),
            // should be transformed to attirbute filter
            CoverageFilter(
                type = CoverageFilter.CoverageFilterType.Exclude,
                defaultMask = CoverageFilter.Any,
                moduleMask = CoverageFilter.Any,
                classMask = "bbb",
                functionMask = CoverageFilter.Any
            ),
            CoverageFilter(
                type = CoverageFilter.CoverageFilterType.Exclude,
                defaultMask = CoverageFilter.Any,
                moduleMask = "ccc",
                classMask = CoverageFilter.Any,
                functionMask = CoverageFilter.Any
            ),
            // should be transformed to attirbute filter
            CoverageFilter(
                type = CoverageFilter.CoverageFilterType.Exclude,
                defaultMask = "zzz",
                moduleMask = CoverageFilter.Any,
                classMask = CoverageFilter.Any,
                functionMask = CoverageFilter.Any
            ),
            CoverageFilter(
                type = CoverageFilter.CoverageFilterType.Exclude,
                defaultMask = CoverageFilter.Any,
                moduleMask = CoverageFilter.Any,
                classMask = CoverageFilter.Any,
                functionMask = "ddd"
            )
        )
        _parametersServiceMock.also {
            every { it.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_ATTRIBUTE_FILTERS) } returns "SOME_ATTRIBUTE_FILTER_VALUE"
        }
        every { _coverageFilterConverterMock.convert(any()) } returns paramFiltersMock

        // act
        val result = _instance.attributeFilters.toList()

        // assert
        assertEquals(
            result,
            sequenceOf<CoverageFilter>(
                CoverageFilter(
                    type = CoverageFilter.CoverageFilterType.Exclude,
                    defaultMask = CoverageFilter.Any,
                    moduleMask = CoverageFilter.Any,
                    classMask = "bbb",
                    functionMask = CoverageFilter.Any
                ),
                CoverageFilter(
                    type = CoverageFilter.CoverageFilterType.Exclude,
                    defaultMask = CoverageFilter.Any,
                    moduleMask = CoverageFilter.Any,
                    classMask = "zzz",
                    functionMask = CoverageFilter.Any
                )
            ).plus(DotCoverFilterProvider.DefaultExcludeAttributeFilters).toList()
        )
    }

    @Test
    fun `should provide default attribute filters when default filters are enabled and no filters from the parameter`() {
        // arrange
        _parametersServiceMock.also {
            every { it.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_ATTRIBUTE_FILTERS) } returns "SOME_FILTER_VALUE"
        }
        every { _coverageFilterConverterMock.convert(any()) } returns emptySequence()

        // act
        val result = _instance.attributeFilters.toList()

        // assert
        assertEquals(result, DotCoverFilterProvider.DefaultExcludeAttributeFilters.toList())
    }

    @Test
    fun `should not provide default attribute filters when default filters are disabled`() {
        // arrange
        _parametersServiceMock.also {
            every { it.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_ATTRIBUTE_FILTERS) } returns "SOME_FILTER_VALUE"
            every { it.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_DOTCOVER_WRAPPER_COVER_DEFAULT_ATTRIBUTE_FILTERS_ENABLED) } returns "false"
        }
        every { _coverageFilterConverterMock.convert(any()) } returns emptySequence()

        // act
        val result = _instance.attributeFilters.toList()

        // assert
        assertEquals(result.size, 0)
        assertEquals(result, emptyList<CoverageFilter>())
    }
}