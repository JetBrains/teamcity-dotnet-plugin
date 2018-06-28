package jetbrains.buildServer.rx

fun <T>subjectOf(): Subject<T> = object : Subject<T> {
    private val _observers: MutableList<Observer<T>> = mutableListOf()
    private var _isCompleted: Boolean = false

    override fun subscribe(observer: Observer<T>): Disposable {
        synchronized(_observers) {
            if (_isCompleted) {
                observer.onComplete()
                return@synchronized
            }

            _observers.add(observer)
        }

        return disposableOf {
            synchronized(_observers) {
                _observers.remove(observer)
            }
        }
    }

    override fun onNext(value: T) =
            synchronized(_observers) {
                for (observer in _observers) {
                    observer.onNext(value)
                }
            }

    override fun onError(error: Exception) =
            synchronized(_observers) {
                for (observer in _observers) {
                    observer.onError(error)
                }

                finish()
            }

    override fun onComplete() =
            synchronized(_observers) {
                for (observer in _observers) {
                    observer.onComplete()
                }

                finish()
            }

    private fun finish() {
        _observers.clear()
        _isCompleted = true
    }
}