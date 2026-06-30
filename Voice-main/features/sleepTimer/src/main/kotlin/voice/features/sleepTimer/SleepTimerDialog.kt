package voice.features.sleepTimer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue.Expanded
import androidx.compose.material3.SheetValue.Hidden
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import voice.core.strings.R as StringsR

@Composable
fun SleepTimerDialog(
  onDismiss: () -> Unit,
  onAcceptSleepTimer: (Int) -> Unit,
  onAcceptSleepEpisodeCount: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  var timerMinutes by remember { mutableStateOf("") }
  var episodeCount by remember { mutableStateOf("") }
  ModalBottomSheet(
    modifier = modifier,
    onDismissRequest = onDismiss,
    sheetState = rememberBottomSheetState(
      initialValue = Hidden,
      enabledValues = setOf(Hidden, Expanded),
    ),
  ) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
      Text(
        modifier = Modifier.fillMaxWidth(),
        text = "定时设置",
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold,
      )
      Spacer(modifier = Modifier.size(20.dp))
      Text(
        text = "定时关闭",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
      )
      Spacer(modifier = Modifier.size(8.dp))
      OutlinedTextField(
        value = timerMinutes,
        onValueChange = { timerMinutes = it.filter { c -> c.isDigit() } },
        label = { Text("输入分钟数") },
        placeholder = { Text("例如：30") },
        suffix = { Text("分钟") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = {
          TextButton(
            onClick = {
              val minutes = timerMinutes.toIntOrNull()
              if (minutes != null && minutes > 0) {
                onAcceptSleepTimer(minutes)
              }
            },
          ) {
            Text("确定", fontWeight = FontWeight.Bold)
          }
        },
      )
      Spacer(modifier = Modifier.size(20.dp))
      HorizontalDivider()
      Spacer(modifier = Modifier.size(20.dp))
      Text(
        text = "定集关闭",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
      )
      Spacer(modifier = Modifier.size(8.dp))
      OutlinedTextField(
        value = episodeCount,
        onValueChange = { episodeCount = it.filter { c -> c.isDigit() } },
        label = { Text("输入集数") },
        placeholder = { Text("例如：3") },
        suffix = { Text("集") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = {
          TextButton(
            onClick = {
              val count = episodeCount.toIntOrNull()
              if (count != null && count > 0) {
                onAcceptSleepEpisodeCount(count)
              }
            },
          ) {
            Text("确定", fontWeight = FontWeight.Bold)
          }
        },
      )
      Spacer(modifier = Modifier.size(32.dp))
    }
  }
}