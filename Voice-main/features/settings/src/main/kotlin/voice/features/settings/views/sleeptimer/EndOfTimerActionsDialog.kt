package voice.features.settings.views.sleeptimer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import voice.core.strings.R as StringsR

@Composable
fun EndOfTimerActionsDialog(
  selectedActions: Set<Int>,
  onConfirm: (Set<Int>) -> Unit,
  onDismiss: () -> Unit,
) {
  var actions by remember { mutableStateOf(selectedActions) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("选择定时结束时要执行的指令") },
    text = {
      Column {
        Row(
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Checkbox(
            checked = actions.contains(1),
            onCheckedChange = { checked ->
              actions = if (checked) actions + 1 else actions - 1
            },
          )
          Text("关闭播放器")
        }
        Row(
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Checkbox(
            checked = actions.contains(2),
            onCheckedChange = { checked ->
              actions = if (checked) actions + 2 else actions - 2
            },
          )
          Text("关机")
        }
        Row(
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Checkbox(
            checked = actions.contains(3),
            onCheckedChange = { checked ->
              actions = if (checked) actions + 3 else actions - 3
            },
          )
          Text("重启")
        }
      }
    },
    confirmButton = {
      TextButton(
        onClick = { onConfirm(actions.ifEmpty { setOf(1) }) },
      ) {
        Text(stringResource(StringsR.string.common_dialog_confirm))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(stringResource(StringsR.string.common_dialog_cancel))
      }
    },
  )
}