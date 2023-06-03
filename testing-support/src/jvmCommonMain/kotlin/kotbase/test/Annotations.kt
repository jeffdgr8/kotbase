@file:JvmName("AnnotationsJvm") // https://youtrack.jetbrains.com/issue/KT-21186
@file:Suppress("unused")

package kotbase.test

actual typealias BeforeClass = org.junit.BeforeClass

actual typealias AfterClass = org.junit.AfterClass

actual annotation class IgnoreApple

actual annotation class IgnoreNative
