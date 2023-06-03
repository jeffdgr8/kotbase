@file:JvmName("DatabaseConfigurationJvm") // https://youtrack.jetbrains.com/issue/KT-21186

package kotbase

public actual var DatabaseConfiguration.encryptionKey: EncryptionKey?
    get() = actual.encryptionKey
    set(value) {
        actual.encryptionKey = value
    }
