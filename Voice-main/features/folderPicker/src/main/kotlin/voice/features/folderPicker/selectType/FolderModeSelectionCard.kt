package voice.features.folderPicker.selectType

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import voice.core.data.folders.FolderType
import voice.core.ui.VoiceTheme
import voice.features.folderPicker.FolderTypeIcon
import voice.core.strings.R as StringsR

@Composable
internal fun FolderModeSelectionCard(
  onFolderModeSelect: (FolderMode) -> Unit,
  selectedFolderMode: FolderMode,
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      FolderMode.entries.forEach { folderMode ->
        val selectFolder = { onFolderModeSelect(folderMode) }
        FolderModeRow(
          selectFolder = selectFolder,
          selectedFolderMode = selectedFolderMode,
          folderMode = folderMode,
        )
      }
    }
  }
}

@Composable
private fun FolderModeRow(
  selectedFolderMode: FolderMode,
  folderMode: FolderMode,
  selectFolder: () -> Unit,
) {
  Row(
    modifier = Modifier
      .clickable(onClick = selectFolder)
      .padding(horizontal = 4.dp, vertical = 6.dp)
      .fillMaxWidth(),
    verticalAlignment = Alignment.Top,
  ) {
    RadioButton(
      selected = selectedFolderMode == folderMode,
      onClick = selectFolder,
      modifier = Modifier.padding(top = 2.dp),
    )
    Column(
      modifier = Modifier
        .weight(1F)
        .padding(start = 8.dp, end = 4.dp),
    ) {
      Text(
        text = stringResource(id = folderMode.title()),
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium,
        maxLines = 4,
        overflow = TextOverflow.Ellipsis,
      )
    }
    FolderTypeIcon(
      folderType = when (folderMode) {
        FolderMode.Audiobooks -> FolderType.Root
        FolderMode.SingleBook -> FolderType.SingleFolder
        FolderMode.Authors -> FolderType.Author
      },
    )
  }
}

@StringRes
private fun FolderMode.title(): Int {
  return when (this) {
    FolderMode.Audiobooks -> StringsR.string.folder_mode_root_title
    FolderMode.SingleBook -> StringsR.string.folder_mode_single_title
    FolderMode.Authors -> StringsR.string.folder_mode_author_title
  }
}

@Composable
@Preview
private fun FolderModeSelectionCardPreview() {
  VoiceTheme {
    FolderModeSelectionCard(
      onFolderModeSelect = {},
      selectedFolderMode = FolderMode.Audiobooks,
    )
  }
}