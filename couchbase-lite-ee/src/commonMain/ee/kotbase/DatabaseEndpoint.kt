package kotbase

/**
 * **ENTERPRISE EDITION API**
 *
 * Database based replication target endpoint.
 */
public expect class DatabaseEndpoint

/**
 * Constructor with the database instance
 *
 * @param database the target database
 */
constructor(database: Database) : Endpoint {

    /**
     * The Database instance
     */
    public val database: Database
}
