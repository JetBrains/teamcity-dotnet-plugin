package jetbrains.buildServer.rx

class NotificationCompleted<T> private constructor() : Notification<T>(NotificationKind.OnCompleted) {
    companion object {
        private val sharedObject = NotificationCompleted<Any>()

        fun <T> completed(): NotificationCompleted<T> {
            @Suppress("UNCHECKED_CAST")
            return sharedObject as NotificationCompleted<T>
        }
    }
}