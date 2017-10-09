package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.runner.Converter

interface MSBuildParameterConverter : Converter<MSBuildParameter, String> {
}