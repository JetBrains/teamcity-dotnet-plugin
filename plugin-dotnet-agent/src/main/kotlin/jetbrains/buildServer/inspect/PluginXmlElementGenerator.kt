

package jetbrains.buildServer.inspect

import jetbrains.buildServer.XmlElement

interface PluginXmlElementGenerator {
    val sourceId: String

    fun generateXmlElement(strValue: String): XmlElement
}