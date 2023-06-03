package kotbase

/**
 * Configuration for opening a database.
 */
public expect class DatabaseConfiguration(
    config: DatabaseConfiguration? = null
) {

    /**
     * Set the canonical path of the directory to store the database in.
     * If the directory doesn't already exist it will be created.
     * If it cannot be created throw an IllegalStateException
     *
     * @param directory the directory
     * @return this.
     * @throws IllegalStateException if the directory does not exist anc cannot be created
     */
    public fun setDirectory(directory: String): DatabaseConfiguration

    /**
     * The path to the directory that contains the database.
     * If this path has not been set explicitly (see: `setDirectory` below),
     * then it is the system default.
     */
    public var directory: String
}
