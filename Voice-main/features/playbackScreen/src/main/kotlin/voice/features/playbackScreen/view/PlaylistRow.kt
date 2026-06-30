package voice.features.playbackScreen.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import voice.core.ui.formatTime
import voice.features.playbackScreen.BookPlayViewState
import kotlin.time.Duration

@Composable
internal fun PlaylistRow(
  chapters: List<BookPlayViewState.ChapterItem>,
  currentChapterIndex: Int,
  sleepTimerActive: Boolean,
  sleepTimerRemaining: Duration?,
  episodeCountdown: Int,
  onChapterClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(modifier) {
    LazyColumn(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        .padding(8.dp),
    ) {
      item {
        Text(
          text = "播放列表",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
      }
      itemsIndexed(chapters) { index, chapter ->
        val isCurrent = index == currentChapterIndex
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onChapterClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "${index + 1}",
            style = MaterialTheme.typography.labelMedium,
            color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.width(24.dp),
          )
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = chapter.name,
              style = MaterialTheme.typography.bodySmall,
              color = if (isCurrent) Color.Red else MaterialTheme.colorScheme.onSurface,
              fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            if (isCurrent && chapter.playedTime > 0L) {
              Text(
                text = "${formatTime(chapter.playedTime)} / ${chapter.duration}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
              )
            }
          }
          Spacer(modifier = Modifier.size(4.dp))
          Text(
            text = chapter.duration,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }
    if (sleepTimerActive && sleepTimerRemaining != null) {
      Text(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(top = 8.dp, end = 8.dp)
          .background(
            color = Color(0x7E000000),
            shape = RoundedCornerShape(20.dp),
          )
          .padding(horizontal = 20.dp, vertical = 16.dp),
        text = formatTime(timeMs = sleepTimerRemaining.inWholeMilliseconds),
        color = Color.White,
      )
    }
  }
}
