

package jetbrains.buildServer.inspect

interface PluginDescriptorsProvider {
    fun getPluginDescriptors(): List<PluginDescriptor>
}