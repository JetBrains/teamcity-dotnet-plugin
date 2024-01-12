

package jetbrains.buildServer.dotnet.commands.msbuild

data class MSBuildParameter(val name: String, val value: String, val type: MSBuildParameterType = MSBuildParameterType.Unknown) {
    override fun toString() = "${type.name} [$name]=[$value]"
}