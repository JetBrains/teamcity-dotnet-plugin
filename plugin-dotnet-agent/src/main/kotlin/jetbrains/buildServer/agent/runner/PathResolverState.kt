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