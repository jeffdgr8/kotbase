package com.udobny.kmm.test

@Target(AnnotationTarget.FUNCTION)
expect annotation class BeforeClass()

@Target(AnnotationTarget.FUNCTION)
expect annotation class AfterClass()

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
expect annotation class IgnoreIos()
