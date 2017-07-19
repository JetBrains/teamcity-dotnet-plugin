package jetbrains.buildServer.runners

import org.omg.CORBA.Object
import java.util.stream.Stream

data class Workflow(
        val commandLines: Sequence<CommandLine> = emptySequence<CommandLine>()) {
}