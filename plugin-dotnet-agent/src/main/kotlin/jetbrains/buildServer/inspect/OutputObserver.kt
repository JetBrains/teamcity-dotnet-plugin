package jetbrains.buildServer.inspect

import jetbrains.buildServer.agent.CommandResultEvent
import jetbrains.buildServer.rx.Observer

interface OutputObserver: Observer<String>