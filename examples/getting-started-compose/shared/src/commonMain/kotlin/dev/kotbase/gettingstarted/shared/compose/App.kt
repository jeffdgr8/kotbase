package dev.kotbase.gettingstarted.shared.compose

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.kotbase.gettingstarted.shared.Log
import dev.kotbase.gettingstarted.shared.SharedDbWork
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun App() {
    val scope = rememberCoroutineScope()
    var inputValue by remember { mutableStateOf("") }
    var replicate by remember { mutableStateOf(false) }
    var job by remember { mutableStateOf<Job?>(null) }
    Column(Modifier.fillMaxWidth()) {
        FlowRow(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            TextField(
                value = inputValue,
                modifier = Modifier.width(IntrinsicSize.Min)
                    .weight(1f),
                label = {
                    Text("Doc update input value")
                }, onValueChange = {
                    inputValue = it
                }
            )
            Button(onClick = {
                job?.cancel()
                job = scope.launch {
                    databaseWork(inputValue, replicate)
                }
            }) {
                Text("Run database work")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Replicate")
                Checkbox(replicate, onCheckedChange = {
                    replicate = !replicate
                })
            }
        }
        LogView()
    }
}

@Composable
fun LogView() {
    val logOutput by Log.output.collectAsState()
    val verticalScroll = rememberScrollState(0)
    val horizontalScroll = rememberScrollState(0)
    LaunchedEffect(logOutput) {
        verticalScroll.animateScrollTo(Int.MAX_VALUE)
    }
    Box(
        modifier = Modifier.fillMaxSize()
            .background(color = Color.LightGray)
    ) {
        Text(
            text = logOutput,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxSize()
                .verticalScroll(verticalScroll)
                .horizontalScroll(horizontalScroll)
                .padding(8.dp)
        )
        Scrollbars(verticalScroll, horizontalScroll)
    }
}

@Composable
expect fun BoxScope.Scrollbars(
    verticalScroll: ScrollState,
    horizontalScroll: ScrollState
)

private const val TAG = "COMPOSE_UI"

private suspend fun databaseWork(inputValue: String, replicate: Boolean) {
    SharedDbWork().run {
        createDb("composeApp-db")
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
