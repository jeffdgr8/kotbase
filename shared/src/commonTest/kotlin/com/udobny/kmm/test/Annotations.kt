@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package com.udobny.kmm.test

@Target(AnnotationTarget.FUNCTION)
expect annotation class BeforeClass()

@Target(AnnotationTarget.FUNCTION)
expect annotation class AfterClass()
