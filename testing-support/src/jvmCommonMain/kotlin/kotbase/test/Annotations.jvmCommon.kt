package kotbase.test

actual typealias BeforeClass = org.junit.BeforeClass

actual typealias AfterClass = org.junit.AfterClass

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
actual annotation class IgnoreApple

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
actual annotation class IgnoreNative
