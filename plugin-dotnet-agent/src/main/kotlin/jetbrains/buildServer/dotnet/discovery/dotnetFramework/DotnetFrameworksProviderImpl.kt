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

package jetbrains.buildServer.dotnet.discovery.dotnetFramework

import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.agent.WindowsRegistry
import org.springframework.cache.annotation.Cacheable

class DotnetFrameworksProviderImpl(
    private val _windowsRegistry: WindowsRegistry,
    private val _registryVisitors: List<DotnetFrameworksWindowsRegistryVisitor>,
    private val _dotnetFrameworkValidator: DotnetFrameworkValidator
)
    : DotnetFrameworksProvider {
    @Cacheable("ListOfDotnetFramework", sync = true)
    override fun getFrameworks() =
            _registryVisitors
                    .asSequence()
                    .flatMap { visitor ->
                        visitor.keys.map { key -> Pair(key, visitor) }
                    }
                    .flatMap { (key, visitor) ->
                        _windowsRegistry.accept(key, visitor, true)
                        visitor.getFrameworks()
                    }
                    .map { framework ->
                        val isValid = _dotnetFrameworkValidator.isValid(framework)
                        LOG.debug("Detected ${if (isValid) "valid" else "invalid"} $framework.")
                        Pair(framework, isValid)
                    }
                    .filter { (_, isValid) ->
                        isValid
                    }
                    .map { (framewrok, _) ->
                        framewrok
                    }
                    .distinct()
                    // Materialize to allow caching
                    .toList()
                    .asSequence()

    companion object {
        private val LOG = Logger.getLogger(DotnetFrameworksProviderImpl::class.java)
    }
}