package voice.features.playbackScreen.view

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import voice.core.strings.R

@Composable
internal fun SkipButton(
  forward: Boolean,
  onClick: () -> Unit,
) {
  TextButton(onClick = onClick) {
    Text(
      text = stringResource(
        id = if (forward) {
          R.string.playback_action_fast_forward
        } else {
          R.string.playback_action_rewind
        },
      ),
      style = MaterialTheme.typography.labelLarge,
      fontWeight = FontWeight.Bold,
    )
  }
}
