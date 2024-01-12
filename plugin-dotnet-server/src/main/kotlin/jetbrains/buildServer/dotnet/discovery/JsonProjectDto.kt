

@file:Suppress("unused")

package jetbrains.buildServer.dotnet.discovery

/**
 * Represents dnx project model.
 */
class JsonProjectDto {
    var testRunner: String? = null
    var configurations: Map<String, Any>? = null
    var frameworks: Map<String, Any>? = null
    var runtimes: Map<String, Any>? = null
}