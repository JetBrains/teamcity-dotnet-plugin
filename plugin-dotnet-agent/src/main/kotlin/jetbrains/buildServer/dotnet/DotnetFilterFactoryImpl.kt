/*
 * Copyright 2000-2023 JetBrains s.r.o.
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

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.dotnet.commands.test.TestRunSettingsFileProvider
import jetbrains.buildServer.dotnet.commands.test.TestsFilterProvider
import jetbrains.buildServer.dotnet.commands.test.splitTests.SplitTestsFilterSettings

class DotnetFilterFactoryImpl(
    private val _testsFilterProvider: TestsFilterProvider,
    private val _splitTestsFilterSettings: SplitTestsFilterSettings,
    private val _testRunSettingsFileProvider: TestRunSettingsFileProvider
) : DotnetFilterFactory {
    override fun createFilter(command: DotnetCommandType): DotnetFilter {
        var filterExpression = _testsFilterProvider.filterExpression
        val isSplitting = _splitTestsFilterSettings.isActive
        if (isSplitting && filterExpression.length > MaxArgSize) {
            val settingsFile = _testRunSettingsFileProvider.tryGet(command)
            if (settingsFile != null) {
                return DotnetFilter("", settingsFile, true)
            }
        }

        return DotnetFilter(filterExpression, null, isSplitting)
    }

    companion object {
        internal const val MaxArgSize = 2048
    }
}