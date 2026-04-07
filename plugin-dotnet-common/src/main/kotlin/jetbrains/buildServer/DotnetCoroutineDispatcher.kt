package jetbrains.buildServer

import jetbrains.buildServer.util.executors.ExecutorsFactory
import jetbrains.buildServer.util.executors.TeamCityThreadPoolExecutor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.asCoroutineDispatcher
import java.io.Closeable
import java.util.concurrent.ExecutorService
import java.util.concurrent.ThreadPoolExecutor
import kotlin.coroutines.CoroutineContext

class DotnetCoroutineDispatcher()
    : Closeable, CoroutineDispatcher() {

    private var _executor: ExecutorService
    private var _dispatcher: ExecutorCoroutineDispatcher

    init {
        _executor = ExecutorsFactory.newFixedDaemonExecutor(".NET Runner", 4)
        _dispatcher = _executor.asCoroutineDispatcher()
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) =
        _dispatcher.dispatch(context, block)

    override fun close() = _executor.shutdown()
}