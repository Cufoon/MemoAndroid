package cufoon.memo.android.util

import timber.log.Timber

class Console {
    fun log(message: String?, vararg args: Any?) {
        Timber.tag("cufoon_log").d(message, *args)
    }

    fun log(t: Throwable?, message: String?, vararg args: Any?) {
        Timber.tag("cufoon_log").d(t, message, *args)
    }

    fun log(t: Throwable?) {
        Timber.tag("cufoon_log").d(t)
    }
}

val console = Console()

fun initConsoleLog() {
    Timber.plant(Timber.DebugTree())
}
