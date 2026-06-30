package voice.features.bookOverview.views

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import voice.core.strings.R as StringsR

@Composable
internal fun SettingsIcon(onSettingsClick: () -> Unit) {
  TextButton(onSettingsClick) {
    Text(stringResource(StringsR.string.settings_action_open))
  }
}
