package kotbase

import org.junit.Test

class AutoInitTest {

    @Test
    fun testCreateDatabaseWithoutInit() {
        val db = Database("succeed", DatabaseConfiguration())
        db.delete()
    }

    @Test
    fun testGetConsoleWithoutInit() {
        Database.log.console
    }

    @Test
    fun testGetFileWithoutInit() {
        Database.log.file
    }

    @Test
    fun testCreateDBConfigWithoutInit() {
        DatabaseConfiguration()
    }
}
