package kotbase

@OptIn(ExperimentalMultiplatform::class)
@AllowDifferentMembersInActual
public actual open class IndexConfiguration
internal constructor(public actual val expressions: List<String>)
