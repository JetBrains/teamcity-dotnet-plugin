package jetbrains.buildServer.dotnet.arguments

import jetbrains.buildServer.dotnet.ArgumentsProvider

interface ArgumentsProviderSource : Sequence<ArgumentsProvider> {
}