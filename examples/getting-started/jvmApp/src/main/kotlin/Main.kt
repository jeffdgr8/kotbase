import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.singleWindowApplication
import dev.kotbase.gettingstarted.shared.Log
import dev.kotbase.gettingstarted.shared.SharedDbWork
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.awt.Dimension

fun main() = singleWindowApplication(title = "Kotbase") {
    window.minimumSize = Dimension(600, 200)
    MaterialTheme {
        App()
    }
}

@Composable
fun App() {
    val scope = rememberCoroutineScope()
    var inputValue by remember { mutableStateOf("") }
    var replicate by remember { mutableStateOf(false) }
    var job by remember { mutableStateOf<Job?>(null) }
    Column(Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            TextField(
                value = inputValue,
                modifier = Modifier.weight(1f),
                label = {
                    Text("Doc update input value")
                }, onValueChange = {
                    inputValue = it
                }
            )
            Spacer(Modifier.padding(8.dp))
            Button(onClick = {
                job?.cancel()
                job = scope.launch {
                    databaseWork(inputValue, replicate)
                }
            }) {
                Text("Run database work")
            }
            Spacer(Modifier.padding(8.dp))
            Text("Replicate")
            Checkbox(replicate, onCheckedChange = {
                replicate = !replicate
            })
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
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd)
                .fillMaxHeight()
                .padding(bottom = 8.dp),
            adapter = rememberScrollbarAdapter(verticalScroll)
        )
        HorizontalScrollbar(
            modifier = Modifier.align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(end = 8.dp),
            adapter = rememberScrollbarAdapter(horizontalScroll)
        )
    }
}

@Preview
@Composable
fun AppPreview() {
    App()
}

private const val TAG = "JVM_APP"

private suspend fun databaseWork(inputValue: String, replicate: Boolean) {
    SharedDbWork().run {
        createDb("jvmApp-db")
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
