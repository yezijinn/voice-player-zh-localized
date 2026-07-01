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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import voice.core.strings.R as StringsR

@Composable
fun DebugLogRow(
  enabled: Boolean,
  onEnabledChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  ListItem(
    modifier = modifier.fillMaxWidth(),
    headlineContent = {
      Text(
        text = "输出运行日志",
        style = MaterialTheme.typography.bodyLarge,
      )
    },
    supportingContent = {
      Text(
        text = "开启后，APP将记录详细运行日志，可能影响性能",
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