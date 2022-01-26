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

package jetbrains.buildServer.dotnet

import java.util.*

enum class TestReportingMode(val id: String) {
    On("On"),
    MultiAdapterPath("MultiAdapterPath"),
    MultiAdapterPath_5_0_103("MultiAdapterPath_5_0_103"),
    Off("Off");

    companion object {
        fun parse(text: String): EnumSet<TestReportingMode> {
            val modes = text.splitToSequence('|').map { id ->
                TestReportingMode.values().singleOrNull { it.id.equals(id.trim(), true) }
            }.toList()

            if (modes.filter { it == null }.any()) {
                return EnumSet.noneOf<TestReportingMode>(TestReportingMode::class.java)
            }

            return EnumSet.copyOf<TestReportingMode>(modes);
        }
    }
}