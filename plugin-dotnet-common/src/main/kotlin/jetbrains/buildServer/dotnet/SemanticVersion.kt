

package jetbrains.buildServer.dotnet

data class SemanticVersion(
        val major: Int,
        val minor: Int,
        val build: Int,
        val buildName: String = "") {

    override fun toString(): String {
        return if (buildName.isEmpty()) {
            "$major.$minor.$build"
        } else {
            "$major.$minor.$build-$buildName"
        }
    }
}