@file:Suppress("NO_ACTUAL_FOR_EXPECT") // https://youtrack.jetbrains.com/issue/KT-42466

package com.udobny.kmp.test

@Target(AnnotationTarget.FUNCTION)
expect annotation class BeforeClass()

@Target(AnnotationTarget.FUNCTION)
expect annotation class AfterClass()

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
expect annotation class IgnoreApple()
