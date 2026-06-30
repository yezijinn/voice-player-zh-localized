package voice.features.folderPicker.addcontent

import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
internal fun SelectFolderButton(
  text: String,
  onClick: () -> Unit,
) {
  FilledTonalButton(
    onClick = onClick,
  ) {
    Text(text = text)
  }
}
