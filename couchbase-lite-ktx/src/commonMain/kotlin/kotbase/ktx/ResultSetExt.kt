package kotbase.ktx

import kotbase.ResultSet

/**
 * Read count result from [selectCount].
 */
public inline fun ResultSet.countResult(): Long =
    allResults().first().getLong(0)
