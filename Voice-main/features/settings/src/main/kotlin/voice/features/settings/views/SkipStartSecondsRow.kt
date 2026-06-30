package voice.features.settings.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import voice.core.strings.R as StringsR

@Composable
internal fun SkipStartSecondsRow(
  skipStartSeconds: Int,
  openDialog: () -> Unit,
) {
  ListItem(
    modifier = Modifier
      .clickable { openDialog() }
      .fillMaxWidth(),
    headlineContent = {
      Text(text = stringResource(StringsR.string.settings_playback_skip_start_title))
    },
    supportingContent = {
      Text(
        text = LocalResources.current.getQuantityString(
          StringsR.plurals.duration_seconds,
          skipStartSeconds,
          skipStartSeconds,
        ),
      )
    },
  )
}

@Composable
internal fun SkipStartSecondsDialog(
  currentSeconds: Int,
  onSecondsConfirm: (Int) -> Unit,
  onDismiss: () -> Unit,
) {
  TimeSettingDialog(
    title = stringResource(StringsR.string.settings_playback_skip_start_title),
    currentSeconds = currentSeconds,
    minSeconds = 0,
    maxSeconds = 120,
    textPluralRes = StringsR.plurals.duration_seconds,
    onSecondsConfirm = onSecondsConfirm,
    onDismiss = onDismiss,
  )
}