package ui.widget

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotbase.ReplicatorActivityLevel
import kotbase.ReplicatorStatus

@Composable
fun ReplicationStatus(
    status: ReplicatorStatus?,
    modifier: Modifier = Modifier
) {
    when (status?.activityLevel) {
        ReplicatorActivityLevel.CONNECTING -> CircularProgressIndicator(modifier = modifier.size(24.dp))
        ReplicatorActivityLevel.BUSY -> CircularProgressIndicator(
            progress = {
                with(status.progress) {
                    if (total == 0L) {
                        0F
                    } else {
                        completed.toFloat() / total
                    }
                }
            },
            modifier = modifier.size(24.dp)
        )
        else -> Spacer(modifier = modifier.size(24.dp))
    }
}
