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

enum class IspectionToolPlatform(val id: String, val displayName: String) {
    WindowsX64("x64", "Windows (x64)"),
    WindowsX86("x86", "Windows (x86)"),
    CrossPlatform("Cross-platform", "Cross-platform");

    companion object {
        fun tryParse(id: String): IspectionToolPlatform? {
            return IspectionToolPlatform.values().singleOrNull() { it.id.equals(id, true) }
        }
    }
}