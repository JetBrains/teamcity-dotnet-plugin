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

package jetbrains.buildServer.inspect

import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.inspect.InspectCodeConstants.RUNNER_DISPLAY_NAME

class InspectionOutputObserver(
        private val _loggerService: LoggerService)
    : OutputObserver {
    override fun onNext(value: kotlin.String) {
        if (NoFiles.equals(value.trim(), false)) {
            _loggerService.writeErrorOutput("$NoFiles If you have C++ projects in your solution, specify the x86 ReSharper CLT platform in the $RUNNER_DISPLAY_NAME build step.")
        }
    }

    override fun onError(error: Exception) { }

    override fun onComplete() { }

    companion object {
        const val NoFiles = "No files to inspect were found."
    }
}