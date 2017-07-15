package jetbrains.buildServer.dotnet

interface DotnetSdkVersionProvider {
    /**
     * Returns cleaned .net core sdk version.
     *
     * @param output is a dotnet --version output.
     * @return cleaned version number.
     */

    fun getSdkVersion(output: String): String
}