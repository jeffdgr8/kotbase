package dev.kotbase.notes

import ui.App
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import domain.replication.ReplicationService
import org.koin.android.ext.android.inject

class NotesActivity : ComponentActivity() {

    private val replicationService: ReplicationService by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            App()
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
