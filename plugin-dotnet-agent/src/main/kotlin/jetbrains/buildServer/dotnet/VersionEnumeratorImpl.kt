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

import jetbrains.buildServer.agent.Version

class VersionEnumeratorImpl : VersionEnumerator {
    override fun <T: Versioned>enumerate(versioned: Sequence<T>): Sequence<Pair<String, T>> = sequence {
        versioned
                .filter { it.version != Version.Empty }
                .groupBy { Version(it.version.major, it.version.minor) }
                .forEach { (version, group) ->
                    val maxVersion = group.maxByOrNull { it.version }!!
                    yield("${version.major}.${version.minor}" to maxVersion)
                    yieldAll(group.map { it.version.toString() to it })
                }
    }.distinctBy { it.first }
}