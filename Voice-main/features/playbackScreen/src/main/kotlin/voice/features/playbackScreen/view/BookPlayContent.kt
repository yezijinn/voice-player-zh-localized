package voice.features.playbackScreen.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import voice.core.data.BookId
import voice.features.playbackScreen.BookPlayViewState
import kotlin.time.Duration

@Composable
internal fun BookPlayContent(
  contentPadding: PaddingValues,
  viewState: BookPlayViewState,
  bookId: BookId,
  onPlayClick: () -> Unit,
  onSeek: (Duration) -> Unit,
  onSkipToNext: () -> Unit,
  onSkipToPrevious: () -> Unit,
  onCurrentChapterClick: () -> Unit,
  useLandscapeLayout: Boolean,
) {
  if (useLandscapeLayout) {
    Row(Modifier.padding(contentPadding)) {
      PlaylistRow(
        chapters = viewState.chapters,
        currentChapterIndex = viewState.currentChapterIndex,
        sleepTimerActive = viewState.sleepTimerActive,
        sleepTimerRemaining = viewState.sleepTimerRemaining,
        episodeCountdown = viewState.episodeCountdown,
        onChapterClick = onCurrentChapterClick,
        modifier = Modifier
          .fillMaxHeight()
          .weight(1F)
          .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
      )
      Column(
        modifier = Modifier
          .fillMaxHeight()
          .weight(1F),
        verticalArrangement = Arrangement.Center,
      ) {
        Spacer(modifier = Modifier.size(20.dp))
        SliderRow(
          duration = viewState.duration,
          playedTime = viewState.playedTime,
          onSeek = onSeek,
        )
        Spacer(modifier = Modifier.size(16.dp))
        PlaybackRow(
          playing = viewState.playing,
          onPlayClick = onPlayClick,
          onSkipToPrevious = onSkipToPrevious,
          onSkipToNext = onSkipToNext,
        )
      }
    }
  } else {
    Column(Modifier.padding(contentPadding)) {
      PlaylistRow(
        chapters = viewState.chapters,
        currentChapterIndex = viewState.currentChapterIndex,
        sleepTimerActive = viewState.sleepTimerActive,
        sleepTimerRemaining = viewState.sleepTimerRemaining,
        episodeCountdown = viewState.episodeCountdown,
        onChapterClick = onCurrentChapterClick,
        modifier = Modifier
          .fillMaxWidth()
          .weight(1F)
          .padding(start = 16.dp, end = 16.dp, top = 8.dp),
      )
      Spacer(modifier = Modifier.size(20.dp))
      SliderRow(
        duration = viewState.duration,
        playedTime = viewState.playedTime,
        onSeek = onSeek,
      )
      Spacer(modifier = Modifier.size(16.dp))
      PlaybackRow(
        playing = viewState.playing,
        onPlayClick = onPlayClick,
        onSkipToPrevious = onSkipToPrevious,
        onSkipToNext = onSkipToNext,
      )
      Spacer(modifier = Modifier.size(24.dp))
    }
  }
}