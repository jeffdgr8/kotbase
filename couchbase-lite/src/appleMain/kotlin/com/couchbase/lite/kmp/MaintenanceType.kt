package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.CBLMaintenanceType
import cocoapods.CouchbaseLite.CBLMaintenanceType.*

public actual enum class MaintenanceType {
    REINDEX,
    COMPACT,
    INTEGRITY_CHECK,
    OPTIMIZE,
    FULL_OPTIMIZE;

    public val actual: CBLMaintenanceType
        get() = when (this) {
            REINDEX -> kCBLMaintenanceTypeReindex
            COMPACT -> kCBLMaintenanceTypeCompact
            INTEGRITY_CHECK -> kCBLMaintenanceTypeIntegrityCheck
            OPTIMIZE -> kCBLMaintenanceTypeOptimize
            FULL_OPTIMIZE -> kCBLMaintenanceTypeFullOptimize
        }
}
