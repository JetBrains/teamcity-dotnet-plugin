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

package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.rx.Observer

 data class PathResolverState(
         public val pathToResolve: Path,
         public val virtualPathObserver: Observer<Path>,
         public val commandToResolve: Path = Path("")) {
  override fun equals(other: Any?): Boolean {
   if (this === other) return true
   if (javaClass != other?.javaClass) return false

   other as PathResolverState

   if (pathToResolve != other.pathToResolve) return false
   if (commandToResolve != other.commandToResolve) return false

   return true
  }

  override fun hashCode(): Int {
   var result = pathToResolve.hashCode()
   result = 31 * result + commandToResolve.hashCode()
   return result
  }
 }