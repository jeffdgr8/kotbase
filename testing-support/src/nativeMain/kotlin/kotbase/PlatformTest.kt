package kotbase

import kotbase.internal.utils.FileUtils
import kotlinx.coroutines.*

actual abstract class PlatformTest {

    companion object {
        const val SCRATCH_DIR_NAME = "cbl_test_scratch"
    }

    actual fun setupPlatform() {
        val console = Database.log.console
        console.level = LogLevel.WARNING
        console.domains = LogDomain.ALL_DOMAINS
    }

    actual val tmpDir: String
        get() = FileUtils.verifyDir("build/cb-tmp/$SCRATCH_DIR_NAME")

    actual fun executeAsync(delayMs: Long, task: () -> Unit) {
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch(Dispatchers.Default) {
            delay(delayMs)
            task()
        }
    }
}
