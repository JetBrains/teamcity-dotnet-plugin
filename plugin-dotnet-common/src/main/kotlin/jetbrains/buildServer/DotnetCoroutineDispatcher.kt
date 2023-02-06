/*
 * Copyright 2000-2023 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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