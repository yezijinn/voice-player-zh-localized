package voice.features.settings.views.sleeptimer

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun EndOfTimerKillAppRow(
  enabled: Boolean,
  onEnabledChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  ListItem(
    modifier = modifier.fillMaxWidth(),
    headlineContent = {
      Text(
        text = "定时结束时关闭播放器",
        style = MaterialTheme.typography.bodyLarge,
      )
    },
    supportingContent = {
      Text(
        text = "定时结束后，延迟3秒关闭APP进程",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    },
    trailingContent = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Switch(
          checked = enabled,
          onCheckedChange = onEnabledChange,
        )
      }
    },
  )
}