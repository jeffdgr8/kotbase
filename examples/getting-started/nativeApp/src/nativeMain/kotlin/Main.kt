import dev.kotbase.gettingstarted.shared.Log
import dev.kotbase.gettingstarted.shared.SharedDbWork
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
    val inputValue = args.firstOrNull() ?: ""
    val replicate = args.getOrNull(1)?.toBoolean() ?: false
    runBlocking {
        databaseWork(inputValue, replicate)
    }
}

private const val TAG = "NATIVE_APP"

private suspend fun databaseWork(inputValue: String, replicate: Boolean) {
    SharedDbWork().run {
        createDb("nativeApp-db")
        val docId = createDoc()
        Log.i(TAG, "Created document :: $docId")
        retrieveDoc(docId)
        updateDoc(docId, inputValue)
        Log.i(TAG, "Updated document :: $docId")
        queryDocs()
        if (replicate) {
            replicate().collect {
                Log.i(TAG, "Replicator Change :: $it")
            }
        }
    }
}