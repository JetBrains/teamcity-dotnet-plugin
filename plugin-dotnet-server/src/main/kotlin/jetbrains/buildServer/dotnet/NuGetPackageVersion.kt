package jetbrains.buildServer.dotnet

data class NuGetPackageVersion(
        public val major: Int,
        public val minor: Int,
        public val build: Int,
        public val buildName: String = "") {

    override fun toString(): String {
        if(buildName.isNullOrEmpty()) {
            return "${major}.${minor}.${build}"
        }
        else {
            return "${major}.${minor}.${build}-${buildName}"
        }
    }
}