package jetbrains.buildServer

import jetbrains.buildServer.util.executors.ExecutorsFactory
import jetbrains.buildServer.util.executors.TeamCityThreadPoolExecutor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.ThreadPoolExecutor
import kotlin.coroutines.CoroutineContext

class DotnetCoroutineDispatcher()
    : CoroutineDispatcher() {

    private var _dispatcher: ExecutorCoroutineDispatcher

    init {
        val executor = ExecutorsFactory.newFixedDaemonExecutor(".NET Runner",  4) as ThreadPoolExecutor
        _dispatcher = executor.asCoroutineDispatcher()
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) =
        _dispatcher.dispatch(context, block)
}