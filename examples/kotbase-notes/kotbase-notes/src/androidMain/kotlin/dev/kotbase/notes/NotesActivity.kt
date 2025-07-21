package dev.kotbase.notes

import ui.App
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import domain.replication.ReplicationService
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.KoinAndroidContext
import org.koin.core.annotation.KoinExperimentalAPI

class NotesActivity : ComponentActivity() {

    private val replicationService: ReplicationService by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            @OptIn(KoinExperimentalAPI::class)
            KoinAndroidContext {
                App()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        replicationService.startReplication()
    }

    override fun onStop() {
        super.onStop()
        replicationService.stopReplication()
    }
}
