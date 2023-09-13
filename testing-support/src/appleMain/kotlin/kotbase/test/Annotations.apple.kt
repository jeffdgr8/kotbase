package kotbase.test

actual typealias BeforeClass = kotlin.test.BeforeClass

actual typealias AfterClass = kotlin.test.AfterClass

actual typealias IgnoreApple = kotlin.test.Ignore

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
actual annotation class IgnoreNative
