package jetbrains.buildServer.dotcover.report

enum class DotCoverVersion(private val myVersionNumber: Int, val displayVersion: String) {

    DotCover_1_0(1, "DotCover 1.0.x"),
    DotCover_1_1(2, "DotCover 1.1.x"),
    DotCover_1_2(3, "DotCover 1.2.x"),
    DotCover_2_0(4, "DotCover 2.0.x"),
    DotCover_2_1(5, "DotCover 2.1.x"),
    DotCover_2_2(6, "DotCover 2.2.x"),
    DotCover_2_5(7, "DotCover 2.5.x"),
    DotCover_2_6(8, "DotCover 2.6.x"),
    DotCover_2_7(9, "DotCover 2.7.x"),
    DotCover_3_0(9, "DotCover 3.0.x"),
    DotCover_3_1(9, "DotCover 3.1.x"),
    DotCover_3_2(10, "DotCover 3.2.x"),
    DotCover_10_0(11, "DotCover 10.0.x"),
    DotCover_2016AndHigher(12, "DotCover 2016 and higher");

    fun isOlder(version: DotCoverVersion): Boolean {
        return myVersionNumber < version.myVersionNumber
    }
}
