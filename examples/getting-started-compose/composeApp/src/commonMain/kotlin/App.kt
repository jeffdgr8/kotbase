import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun App() {
    MaterialTheme {
        val scope = rememberCoroutineScope()
        var inputValue by remember { mutableStateOf("") }
        var replicate by remember { mutableStateOf(false) }
        var job by remember { mutableStateOf<Job?>(null) }
        Column(Modifier.fillMaxWidth()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                TextField(
                    value = inputValue,
                    modifier = Modifier.width(IntrinsicSize.Min)
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                    label = {
                        Text("Doc update input value")
                    },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    onValueChange = {
                        inputValue = it
                    }
                )
                Button(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    onClick = {
                        job?.cancel()
                        job = scope.launch {
                            databaseWork(inputValue, replicate)
                        }
                    }
                ) {
                    Text("Run database work")
                }
                Row(modifier = Modifier.align(Alignment.CenterVertically),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Replicate")
                    Switch(replicate, onCheckedChange = {
                        replicate = !replicate
                    })
                }
            }
            LogView()
        }
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

private const val TAG = "COMPOSE_APP"

private val sharedDbWork = SharedDbWork()

private suspend fun databaseWork(inputValue: String, replicate: Boolean) {
    sharedDbWork.run {
        createDb("composeApp-db")
        createCollection("example-coll")
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
