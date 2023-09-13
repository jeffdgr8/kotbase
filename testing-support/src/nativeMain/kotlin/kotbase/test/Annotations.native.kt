package kotbase.test

actual typealias BeforeClass = kotlin.test.BeforeClass

actual typealias AfterClass = kotlin.test.AfterClass

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
actual annotation class IgnoreApple

actual typealias IgnoreNative = kotlin.test.Ignore
