package jetbrains.buildServer.dotnet.coverage.dotcover

import jetbrains.buildServer.dotnet.coverage.serviceMessage.DotnetCoverageParameters
import org.jdom.Content
import org.jdom.Document
import org.jdom.Element
import java.io.File

@Deprecated("Deprecated after task TW-85039. Needed for backward compatibility")
class DotCover2016CommandsConfigFactory(
    coverageParameters: DotnetCoverageParameters
) : DotCover27CommandsConfigFactory(coverageParameters) {

    override fun createDeleteCommandConfig(sources: Collection<File>): Document {
        return Document(
            object : Element("DeleteParams") {
                init {
                    for (file in sources) {
                        addContent(object : Element("Source") {
                            init {
                                text = file.path
                            }
                        } as Content)
                    }
                }
            }
        )
    }
}
