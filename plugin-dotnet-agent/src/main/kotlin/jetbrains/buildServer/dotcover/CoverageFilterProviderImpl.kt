package jetbrains.buildServer.dotcover

import jetbrains.buildServer.dotnet.DotCoverConstants
import jetbrains.buildServer.runners.Converter
import jetbrains.buildServer.runners.ParameterType
import jetbrains.buildServer.runners.ParametersService
import java.util.*
import kotlin.coroutines.experimental.buildSequence

class CoverageFilterProviderImpl(
        private val _parametersService: ParametersService,
        private val _coverageFilterConverter: Converter<String, Sequence<CoverageFilter>>)
    : CoverageFilterProvider {
    override val filters: Sequence<CoverageFilter>
        get() {
            val filtersStr = _parametersService.tryGetParameter(ParameterType.Runner, DotCoverConstants.PARAM_FILTERS)
            val filters = ArrayList<CoverageFilter>()

            if (filtersStr != null) {
                for (filter in _coverageFilterConverter.convert(filtersStr).map { toModuleFilter(it) }) {
                    filters.add(filter)
                }
            }

            if (filters.size == 0) {
                filters.addAll(0, ourDefaultIncludeFilters)
            }

            addAdditionalAnyFilterWhenHasOutdatedAnyFilter(filters, CoverageFilter.CoverageFilterType.Include)
            addAdditionalAnyFilterWhenHasOutdatedAnyFilter(filters, CoverageFilter.CoverageFilterType.Exclude)
            return filters.asSequence()
        }

    override val attributeFilters: Sequence<CoverageFilter>
        get() {
            val filtersStr = _parametersService.tryGetParameter(ParameterType.Runner, DotCoverConstants.PARAM_ATTRIBUTE_FILTERS)
            val filters = ArrayList<CoverageFilter>()
            if (filtersStr != null) {
                for (filter in _coverageFilterConverter.convert(filtersStr).map {  toAttributeFilter(it) }) {
                    if (filter.type == CoverageFilter.CoverageFilterType.Exclude && CoverageFilter.Any != filter.classMask) {
                        filters.add(filter)
                    }
                }
            }

            filters.addAll(ourDefaultAttributeFilters)
            return filters.asSequence()
        }

    private fun addAdditionalAnyFilterWhenHasOutdatedAnyFilter(filters: MutableList<CoverageFilter>, type: CoverageFilter.CoverageFilterType) {
        val outdatedFilter = CoverageFilter(type, CoverageFilter.Any, OUTDATED_ANY_VAL, CoverageFilter.Any, CoverageFilter.Any)
        val additionalFilter = CoverageFilter(type, CoverageFilter.Any, CoverageFilter.Any, CoverageFilter.Any, CoverageFilter.Any)
        val anyIncludeFilter = filters.indexOf(outdatedFilter)
        if (anyIncludeFilter >= 0) {
            filters.add(anyIncludeFilter, additionalFilter)
        }
    }

    private fun getMask(mask: String, defaultMask: String): String {
        if (CoverageFilter.Any == mask) {
            return defaultMask
        }

        return mask
    }

    private fun toModuleFilter(filter: CoverageFilter): CoverageFilter {
        return CoverageFilter(
                filter.type,
                CoverageFilter.Any,
                getMask(filter.moduleMask, filter.defaultMask),
                filter.classMask,
                filter.functionMask)
    }

    private fun toAttributeFilter(filter: CoverageFilter): CoverageFilter {
        return CoverageFilter(
                filter.type,
                CoverageFilter.Any,
                CoverageFilter.Any,
                getMask(filter.classMask, filter.defaultMask),
                CoverageFilter.Any)
    }

    companion object {
        private val OUTDATED_ANY_VAL = "*.*"
        private val EXCLUDE_FROM_CODE_COVERAGE_ATTRIBUTE = "System.Diagnostics.CodeAnalysis.ExcludeFromCodeCoverageAttribute"
        private val ourDefaultIncludeFilters = Collections.unmodifiableList(Arrays.asList(CoverageFilter(CoverageFilter.CoverageFilterType.Include, CoverageFilter.Any, CoverageFilter.Any, CoverageFilter.Any, CoverageFilter.Any)))
        private val ourDefaultAttributeFilters = Collections.unmodifiableList(Arrays.asList(CoverageFilter(CoverageFilter.CoverageFilterType.Exclude, CoverageFilter.Any, CoverageFilter.Any, EXCLUDE_FROM_CODE_COVERAGE_ATTRIBUTE, CoverageFilter.Any)))
    }
}