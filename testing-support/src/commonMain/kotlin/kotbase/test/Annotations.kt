package kotbase.test

@Target(AnnotationTarget.FUNCTION)
expect annotation class BeforeClass()

@Target(AnnotationTarget.FUNCTION)
expect annotation class AfterClass()

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
expect annotation class IgnoreApple()

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
expect annotation class IgnoreNative()