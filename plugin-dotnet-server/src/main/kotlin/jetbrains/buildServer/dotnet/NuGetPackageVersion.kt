package jetbrains.buildServer.dotnet

data class NuGetPackageVersion(
        private val major: Int,
        private val minor: Int,
        val build: Int,
        private val buildName: String = "") {

    override fun toString(): String {
        return if(buildName.isEmpty()) {
            "$major.$minor.$build"
        }
        else {
            "$major.$minor.$build-$buildName"
        }
    }
}