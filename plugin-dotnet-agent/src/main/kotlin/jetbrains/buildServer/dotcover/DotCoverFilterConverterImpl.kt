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

package jetbrains.buildServer.dotcover

import jetbrains.buildServer.RunBuildException
import java.util.regex.Pattern

class DotCoverFilterConverterImpl : DotCoverFilterConverter {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun convert(filterString: String) = sequence {
        if (filterString.isBlank()) {
            return@sequence
        }

        for (line in ourEndOfLinePattern.split(filterString)) {
            if (line.isNullOrBlank()) {
                continue
            }

            val filterMatcher = ourFilterPattern.matcher(line)
            if (!filterMatcher.find()) {
                throwRunBuildException(line)
            }

            if (filterMatcher.groupCount() != 2) {
                throwRunBuildException(filterMatcher.group())
            }

            val typeStr = filterMatcher.group(FILTER_TYPE_GROUP)
            val filterPartsStr = filterMatcher.group(FILTER_BODY_GROUP)
            if (typeStr.isNullOrBlank() || filterPartsStr.isNullOrBlank()) {
                throwRunBuildException(line)
            }

            val filterPartMatcher = ourFilterPartPattern.matcher(filterPartsStr)
            val partsValue = arrayOfNulls<String>(FUNCTION_VAR + 1)

            while (filterPartMatcher.find()) {
                if (filterPartMatcher.groupCount() != 3) {
                    throwRunBuildException(line)
                }

                val partStr = filterPartMatcher.group(NAMED_PART_TYPE_GROUP)
                var valStr: String? = filterPartMatcher.group(NAMED_PART_VALUE_GROUP)
                if (valStr == null) {
                    valStr = filterPartMatcher.group(VALUE_GROUP)
                }

                if (valStr == null) {
                    throwRunBuildException(line)
                }

                if ("module".equals(partStr, ignoreCase = true) || "assembly".equals(partStr, ignoreCase = true)) {
                    partsValue[MODULE_VAR] = valStr
                    continue
                }

                if ("class".equals(partStr, ignoreCase = true)
                        || "type".equals(partStr, ignoreCase = true)
                        || "attribute".equals(partStr, ignoreCase = true)
                        || "attributename".equals(partStr, ignoreCase = true)) {
                    partsValue[CLASS_VAR] = valStr
                    continue
                }

                if ("function".equals(partStr, ignoreCase = true) || "method".equals(partStr, ignoreCase = true)) {
                    partsValue[FUNCTION_VAR] = valStr
                    continue
                }

                if (partsValue[DEFAULT_VAR] != null) {
                    throwRunBuildException(line)
                }

                partsValue[DEFAULT_VAR] = valStr
            }

            if (partsValue[DEFAULT_VAR] == null && partsValue[MODULE_VAR] == null && partsValue[CLASS_VAR] == null && partsValue[FUNCTION_VAR] == null) {
                throwRunBuildException(line)
            }

            yield(CoverageFilter(
                    when (typeStr) {
                        "-" -> CoverageFilter.CoverageFilterType.Exclude
                        else -> CoverageFilter.CoverageFilterType.Include
                    },
                    partsValue[DEFAULT_VAR] ?: CoverageFilter.Any,
                    partsValue[MODULE_VAR] ?: CoverageFilter.Any,
                    partsValue[CLASS_VAR] ?: CoverageFilter.Any,
                    partsValue[FUNCTION_VAR] ?: CoverageFilter.Any))
        }


        /* if (filters.size == 0) {
             throwRunBuildException(filterString)
         }

         return filters.asSequence()*/
    }

    private fun throwRunBuildException(filter: String) {
        throw RunBuildException("Invalid statement for filter: \"$filter\"")
    }

    companion object {
        private const val DEFAULT_VAR = 0
        private const val MODULE_VAR = 1
        private const val CLASS_VAR = 2
        private const val FUNCTION_VAR = 3
        private const val NAMED_PART_TYPE_GROUP = 1
        private const val NAMED_PART_VALUE_GROUP = 2
        private const val VALUE_GROUP = 3
        private const val FILTER_TYPE_GROUP = 1
        private const val FILTER_BODY_GROUP = 2
        private val ourFilterPattern = Pattern.compile("^\\s*([-|+]\\s*)\\s*:\\s*([^:\\s]+)\\s*\\z", Pattern.CASE_INSENSITIVE)
        private val ourFilterPartPattern = Pattern.compile("(module|assembly|class|type|attribute|attributename|function|method)[=]([^;=]+)|([^;=]+)", Pattern.CASE_INSENSITIVE)
        private val ourEndOfLinePattern = Pattern.compile("\\n")
    }
}