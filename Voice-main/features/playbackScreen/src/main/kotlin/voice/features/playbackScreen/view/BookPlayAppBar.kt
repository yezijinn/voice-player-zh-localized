package voice.features.playbackScreen.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import voice.core.strings.R
import voice.features.playbackScreen.BookPlayViewState

@Composable
internal fun BookPlayAppBar(
  viewState: BookPlayViewState,
  onSleepTimerClick: () -> Unit,
  onBookmarkClick: () -> Unit,
  onBookmarkLongClick: () -> Unit,
  onSpeedChangeClick: () -> Unit,
  onSettingsClick: () -> Unit,
  onCloseClick: () -> Unit,
  useLandscapeLayout: Boolean,
) {
  val appBarActions: @Composable () -> Unit = {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 4.dp),
      horizontalArrangement = Arrangement.SpaceEvenly,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      TextButton(onClick = onCloseClick) {
        Text(
          text = stringResource(id = R.string.common_action_home),
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
        )
      }
      TextButton(onClick = onSleepTimerClick) {
        Text(
          text = if (viewState.timerActive) "定时:开" else "定时:关",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
        )
      }
      TextButton(onClick = onBookmarkClick) {
        Text(
          text = stringResource(id = R.string.bookmark_title),
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
        )
      }
      TextButton(onClick = onSpeedChangeClick) {
        Text(
          text = stringResource(id = R.string.playback_speed_title),
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
        )
      }
      TextButton(onClick = onSettingsClick) {
        Text(
          text = stringResource(id = R.string.settings_action_open),
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
        )
      }
    }
  }
  if (useLandscapeLayout) {
    TopAppBar(
      title = {
        AppBarTitle(viewState.title)
      },
      actions = {
        appBarActions()
      },
    )
  } else {
    LargeTopAppBar(
      title = {
        AppBarTitle(viewState.title)
      },
      actions = {
        appBarActions()
      },
    )
  }
}