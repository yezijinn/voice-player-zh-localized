package voice.features.bookOverview.views

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import voice.core.strings.R as StringsR

@Composable
internal fun BookFolderIcon(
  withHint: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(modifier) {
    TextButton(onClick = onClick) {
      Text(stringResource(StringsR.string.library_folders_title))
    }
    if (withHint) {
      AddBookHint()
    }
  }
}
