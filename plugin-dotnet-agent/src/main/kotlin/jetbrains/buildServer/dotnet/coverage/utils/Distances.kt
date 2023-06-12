package jetbrains.buildServer.dotnet.coverage.utils

object Distances {

    /**
     * Computes V.Levenstain (aka edit distance) dinstance between strings.
     * This distance could defind metric space on a space of strings.
     *
     * [http://en.wikipedia.org/wiki/Levenshtein_distance](http://en.wikipedia.org/wiki/Levenshtein_distance)
     *
     * @param s string
     * @param t string
     * @return information distance between s and t
     */
    fun levenshteinDistance(s: String, t: String): Int {
        val m = s.length
        val n = t.length
        // d is a table with m+1 rows and n+1 columns
        val d = Array(m + 1) { IntArray(n + 1) }

        for (i in 0..m) d[i][0] = i // deletion
        for (j in 0..n) d[0][j] = j // insertion

        for (j in 1..n) {
            for (i in 1..m) {
                if (s[i - 1] == t[j - 1]) {
                    d[i][j] = d[i - 1][j - 1]
                } else {
                    d[i][j] = Math.min(
                        Math.min(
                            d[i - 1][j],  // deletion
                            d[i][j - 1]),  // insertion
                        d[i - 1][j - 1] // substitution
                    ) + 1
                }
            }
        }
        return d[m][n]
    }
}
