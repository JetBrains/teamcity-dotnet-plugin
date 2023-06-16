package jetbrains.buildServer.dotnet.coverage

import jetbrains.buildServer.agent.DataProcessor
import jetbrains.buildServer.agent.DataProcessorContext
import jetbrains.buildServer.dotnet.CoverageConstants

class DotnetCoverageDataProcessor(private val myProcessor: DotnetCoverageProcessor) : DataProcessor {

    override fun getType(): String {
        return CoverageConstants.COVERAGE_TYPE
    }

    override fun processData(context: DataProcessorContext) {
        context.arguments["tool"]?.let {
            myProcessor.addCoverageReport(it, context.file)
        }
    }
}
