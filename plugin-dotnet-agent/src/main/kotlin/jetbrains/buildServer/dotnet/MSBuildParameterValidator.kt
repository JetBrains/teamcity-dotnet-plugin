package jetbrains.buildServer.dotnet

interface MSBuildParameterValidator {
    fun isValid(parameter: MSBuildParameter): Boolean
}