package voice.features.folderPicker

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import voice.core.data.folders.FolderType
import voice.core.strings.R as StringsR

@Composable
internal fun FolderTypeIcon(folderType: FolderType) {
  Text(
    text = folderType.label(),
    style = MaterialTheme.typography.labelSmall,
    color = LocalContentColor.current.copy(alpha = 0.6F),
  )
}

@Composable
private fun FolderType.label(): String = when (this) {
  FolderType.SingleFile -> stringResource(StringsR.string.folder_mode_single_title)
  FolderType.SingleFolder -> stringResource(StringsR.string.folder_mode_single_title)
  FolderType.Root -> stringResource(StringsR.string.folder_mode_root_title)
  FolderType.Author -> stringResource(StringsR.string.folder_mode_author_title)
}
