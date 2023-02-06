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

package jetbrains.buildServer.script.discovery

import jetbrains.buildServer.dotnet.discovery.DiscoveredTarget
import jetbrains.buildServer.dotnet.discovery.StreamFactory
import jetbrains.buildServer.dotnet.discovery.StreamFactoryImpl
import jetbrains.buildServer.script.ScriptConstants
import jetbrains.buildServer.script.ScriptType
import jetbrains.buildServer.serverSide.discovery.BreadthFirstRunnerDiscoveryExtension
import jetbrains.buildServer.serverSide.discovery.DiscoveredObject
import jetbrains.buildServer.util.browser.Element

class CSharpScriptDiscoveryExtension(
        private val _scriptDiscover: ScriptDiscover)
    : BreadthFirstRunnerDiscoveryExtension() {
    override fun discoverRunnersInDirectory(dir: Element, filesAndDirs: MutableList<Element>) =
            discover(StreamFactoryImpl(dir.browser), getElements(filesAndDirs.asSequence()).map { it.fullName }).toMutableList()

    fun discover(streamFactory: StreamFactory, paths: Sequence<String>) =
        _scriptDiscover.discover(streamFactory, paths)
                .map {
                    DiscoveredTarget(
                            ScriptConstants.RUNNER_TYPE,
                            "Run ${it.path}",
                            mapOf(
                                    ScriptConstants.SCRIPT_TYPE to ScriptType.File.id,
                                    ScriptConstants.SCRIPT_FILE to it.path,
                                    ScriptConstants.CLT_PATH to "%teamcity.tool.${ScriptConstants.CLT_TOOL_TYPE_ID}.DEFAULT%"
                            )
                    )
                }

    private fun getElements(elements: Sequence<Element>, depth: Int = 3): Sequence<Element> =
            if (depth > 0)
                elements.filter { it.isLeaf && it.isContentAvailable }.plus(elements.filter { !it.isLeaf && it.children != null }.flatMap { getElements(it.children!!.asSequence(), depth - 1) })
            else
                emptySequence()
}