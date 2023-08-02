package kotbase

public actual class DatabaseChange
internal constructor(
    public actual val database: Database,
    public actual val documentIDs: List<String>
)
