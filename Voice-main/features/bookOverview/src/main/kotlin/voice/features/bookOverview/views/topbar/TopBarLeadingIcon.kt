package voice.features.bookOverview.views.topbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import voice.core.strings.R

@Composable
internal fun ColumnScope.TopBarLeadingIcon(
  searchActive: Boolean,
  onActiveChange: (Boolean) -> Unit,
) {
  AnimatedVisibility(
    visible = searchActive,
    enter = fadeIn(),
    exit = fadeOut(),
  ) {
    TextButton(onClick = { onActiveChange(false) }) {
      Text(stringResource(id = R.string.common_action_close))
    }
  }
  AnimatedVisibility(
    visible = !searchActive,
    enter = fadeIn(),
    exit = fadeOut(),
  ) {
    Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
      Text(
        text = stringResource(id = R.string.library_search_hint),
      )
    }
  }
}
