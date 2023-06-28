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

package jetbrains.buildServer.dotnet.commands.resolution.resolvers.transformation

import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.dotnet.DotnetCommand
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.commands.resolution.DotnetCommandsStream
import jetbrains.buildServer.dotnet.commands.test.splitting.TestsSplittingMode
import jetbrains.buildServer.dotnet.commands.test.splitting.TestsSplittingSettings

class TestClassTestsSplittingCommandsResolver(
    private val _testsSplittingSettings: TestsSplittingSettings,
    private val _loggerService: LoggerService,
) : TestsSplittingCommandsResolverBase(_testsSplittingSettings, _loggerService) {
    override fun shouldBeApplied(commands: DotnetCommandsStream) =
        super.shouldBeApplied(commands) && _testsSplittingSettings.mode == TestsSplittingMode.TestClassNameFilter

    override val requirementsMessage: String = DotnetConstants.PARALLEL_TESTS_FEATURE_WITH_FILTER_REQUIREMENTS_MESSAGE

    override protected fun transform(testCommand: DotnetCommand) = sequence {
        yield(testCommand)
    }
}

