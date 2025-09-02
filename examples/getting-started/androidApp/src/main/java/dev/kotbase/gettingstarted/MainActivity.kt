package dev.kotbase.gettingstarted

import SharedDbWork
import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import dev.kotbase.gettingstarted.databinding.ActivityMainBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var inputValue: String = ""
    private var replicate = false
    private var job: Job? = null

    private val handler = Handler(Looper.getMainLooper())
    private var scrollAnimator: ObjectAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val i = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            view.setPadding(i.left, i.top, i.right, i.bottom)
            WindowInsetsCompat.CONSUMED
        }

        binding.runDbWork.setOnClickListener {
            job?.cancel()
            job = lifecycleScope.launch {
                databaseWork(inputValue, replicate)
            }
        }
        binding.inputValue.editText?.addTextChangedListener {
            inputValue = it.toString()
        }
        binding.replicate.setOnCheckedChangeListener { _, isChecked ->
            replicate = isChecked
        }
        lifecycleScope.launch {
            Log.output.collect {
                binding.logView.text = it

                scrollAnimator?.cancel()
                handler.post {
                    scrollAnimator = ObjectAnimator.ofInt(binding.logViewScroll, "scrollY", binding.logView.height).apply {
                        doOnEnd { scrollAnimator = null }
                        start()
                    }
                }
            }
        }
    }
}

private const val TAG = "ANDROID_APP"

private val sharedDbWork = SharedDbWork()

private suspend fun databaseWork(inputValue: String, replicate: Boolean) {
    sharedDbWork.run {
        createDb("androidApp-db")
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
