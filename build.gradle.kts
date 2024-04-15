buildscript {
    val composeVersion by extra("1.5.4")
    // https://square.github.io/okhttp/changelogs/changelog/
    val okhttpVersion by extra("4.12.0")
}

plugins {
    id("com.android.application") version "8.3.2" apply false
    id("com.android.library") version "8.3.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.23" apply false
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.23" apply false
}

val cufoonProject by extra("cufoon")

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
