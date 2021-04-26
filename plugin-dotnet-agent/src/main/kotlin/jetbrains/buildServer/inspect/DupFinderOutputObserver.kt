package jetbrains.buildServer.inspect

class DupFinderOutputObserver: OutputObserver {
    override fun onNext(value: String) { }

    override fun onError(error: Exception) { }

    override fun onComplete() { }
}