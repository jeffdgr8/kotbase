@file:JvmName("AnnotationsAndroidAndroidTest") // https://youtrack.jetbrains.com/issue/KT-21186
@file:Suppress("unused")

package com.udobny.kmp.test

actual typealias BeforeClass = org.junit.BeforeClass

actual typealias AfterClass = org.junit.AfterClass

actual annotation class IgnoreApple
