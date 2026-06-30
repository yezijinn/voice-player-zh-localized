package voice.features.settings.views.sleeptimer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import voice.core.ui.VoiceTheme
import voice.features.settings.SettingsListener
import voice.features.settings.SettingsViewState
import voice.core.strings.R as StringsR

@Composable
internal fun AutoSleepTimerCard(
  viewState: SettingsViewState.AutoSleepTimerViewState,
  listener: SettingsListener,
) {
  OutlinedCard(modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp)) {
    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
      AutoSleepTimerRow(
        autoSleepTimer = viewState.enabled,
        start = viewState.startTime,
        end = viewState.endTime,
        toggleAutoSleepTimer = listener::setAutoSleepTimer,
      )
      Row(Modifier.padding(start = 8.dp)) {
        AutoSleepTimerSetting(
          time = viewState.startTime,
          label = stringResource(StringsR.string.settings_auto_sleep_timer_start_label),
          enabled = viewState.enabled,
          setAutoSleepTime = listener::setAutoSleepTimerStart,
        )
        AutoSleepTimerSetting(
          time = viewState.endTime,
          label = stringResource(StringsR.string.settings_auto_sleep_timer_end_label),
          enabled = viewState.enabled,
          setAutoSleepTime = listener::setAutoSleepTimerEnd,
        )
      }
      Row(Modifier.padding(start = 8.dp)) {
        var showDurationDialog by remember { mutableStateOf(false) }
        TextButton(
          enabled = viewState.enabled,
          onClick = { showDurationDialog = true },
        ) {
          Text("设置播放时长 (${viewState.durationMinutes}分钟)")
        }
        if (showDurationDialog) {
          DurationPickerDialog(
            initialMinutes = viewState.durationMinutes,
            onConfirm = { minutes ->
              listener.setAutoSleepTimerDuration(minutes)
              showDurationDialog = false
            },
            onDismiss = { showDurationDialog = false },
          )
        }
      }
    }
  }
}

@Composable
private fun DurationPickerDialog(
  initialMinutes: Int,
  onConfirm: (Int) -> Unit,
  onDismiss: () -> Unit,
) {
  var minutes by remember { mutableStateOf(initialMinutes.toString()) }
  androidx.compose.material3.AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("设置播放时长") },
    text = {
      androidx.compose.material3.OutlinedTextField(
        value = minutes,
        onValueChange = { minutes = it.filter { c -> c.isDigit() } },
        label = { Text("分钟数") },
        singleLine = true,
      )
    },
    confirmButton = {
      TextButton(
        onClick = {
          val value = minutes.toIntOrNull() ?: initialMinutes
          onConfirm(value.coerceAtLeast(1))
        },
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

@Composable
@Preview
private fun AutoSleepTimerCardPreview() {
  VoiceTheme {
    AutoSleepTimerCard(SettingsViewState.AutoSleepTimerViewState.preview(), SettingsListener.noop())
  }
}
