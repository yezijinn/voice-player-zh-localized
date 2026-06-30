package voice.features.playbackScreen.view

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import voice.core.strings.R

@Composable
internal fun CloseIcon(onCloseClick: () -> Unit) {
  TextButton(onClick = onCloseClick) {
    Text(text = stringResource(id = R.string.common_action_close))
  }
}
