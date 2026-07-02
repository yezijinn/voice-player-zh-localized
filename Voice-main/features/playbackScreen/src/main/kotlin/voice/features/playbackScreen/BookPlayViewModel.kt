package voice.features.playbackScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import voice.core.common.DispatcherProvider
import voice.core.common.MainScope
import voice.core.data.Book
import voice.core.data.BookId
import voice.core.data.ChapterDurationHelper
import voice.core.data.KioskModeDemoData
import voice.core.data.Chapter
import voice.core.data.durationMs
import voice.core.data.markForPosition
import voice.core.data.repo.BookRepository
import voice.core.data.repo.BookmarkRepo
import voice.core.data.sleeptimer.SleepTimerPreference
import voice.core.data.store.CurrentBookStore
import voice.core.data.store.SkipEndSecondsStore
import voice.core.data.store.SkipSilenceStore
import voice.core.data.store.SkipStartSecondsStore
import voice.core.data.store.SleepTimerPreferenceStore
import voice.core.featureflag.ExperimentalPlaybackPersistenceQualifier
import voice.core.featureflag.FeatureFlag
import voice.core.featureflag.KioskModeFeatureFlagQualifier
import voice.core.logging.api.Logger
import voice.core.playback.CurrentBookResolver
import voice.core.playback.PlayerController
import voice.core.playback.misc.Decibel
import voice.core.playback.misc.VolumeGain
import voice.core.playback.overlay
import voice.core.playback.playstate.PlayStateManager
import voice.core.sleeptimer.SleepTimer
import voice.core.sleeptimer.SleepTimerMode
import voice.core.sleeptimer.SleepTimerState
import voice.core.ui.formatTime
import voice.features.playbackScreen.batteryOptimization.BatteryOptimization
import voice.navigation.Destination
import voice.navigation.Navigator
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

@AssistedInject
class BookPlayViewModel(
  private val bookRepository: BookRepository,
  private val currentBookResolver: CurrentBookResolver,
  private val player: PlayerController,
  private val sleepTimer: SleepTimer,
  private val playStateManager: PlayStateManager,
  @CurrentBookStore
  private val currentBookStoreId: DataStore<BookId?>,
  private val navigator: Navigator,
  private val bookmarkRepository: BookmarkRepo,
  private val volumeGainFormatter: VolumeGainFormatter,
  private val batteryOptimization: BatteryOptimization,
  dispatcherProvider: DispatcherProvider,
  @SleepTimerPreferenceStore
  private val sleepTimerPreferenceStore: DataStore<SleepTimerPreference>,
  @SkipSilenceStore
  private val skipSilenceStore: DataStore<Boolean>,
  @ExperimentalPlaybackPersistenceQualifier
  private val experimentalPlaybackPersistenceFeatureFlag: FeatureFlag<Boolean>,
  @KioskModeFeatureFlagQualifier
  private val kioskModeFeatureFlag: FeatureFlag<Boolean>,
  @SkipStartSecondsStore
  private val skipStartSecondsStore: DataStore<Int>,
  @SkipEndSecondsStore
  private val skipEndSecondsStore: DataStore<Int>,
  @Assisted
  private val bookId: BookId,
) {

  private val scope = MainScope(dispatcherProvider)

  internal val viewEffects: Flow<BookPlayViewEffect>
    field = MutableSharedFlow<BookPlayViewEffect>(extraBufferCapacity = 1)

  internal val dialogState: State<BookPlayDialogViewState?>
    field = mutableStateOf<BookPlayDialogViewState?>(null)

  internal val showSleepTimerDialog = mutableStateOf(false)

  private var episodeCountdown by mutableStateOf(0)
  private var lastChapterIndex = -1

  init {
    scope.launch {
      player.pauseIfCurrentBookDifferentFrom(bookId)
      currentBookStoreId.updateData { bookId }
    }
  }

  @Composable
  fun viewState(): BookPlayViewState? {
    val kioskMode = remember { kioskModeFeatureFlag.get() }
    if (kioskMode) return kioskModeViewState()

    val persistedBook = remember(bookId) {
      bookRepository.flow(bookId).filterNotNull()
    }.collectAsState(initial = null).value ?: return null

    val skipSilence by remember { skipSilenceStore.data }.collectAsState(initial = false)
    LaunchedEffect(skipSilence) {
      player.skipSilence(skipSilence)
    }

    val experimentalPlaybackPersistence = experimentalPlaybackPersistenceFeatureFlag.get()
    val livePlaybackState = if (experimentalPlaybackPersistence) {
      remember(bookId) { player.livePlaybackStateFlow(bookId) }
        .collectAsState(null).value
    } else {
      null
    }
    val managerPlayState by remember {
      playStateManager.playStateFlow
    }.collectAsState()

    val book = if (livePlaybackState != null) {
      persistedBook.overlay(livePlaybackState)
    } else {
      persistedBook
    }
    val isPlaying = livePlaybackState?.isPlaying ?: (managerPlayState == PlayStateManager.PlayState.Playing)

    val currentMark = book.currentChapter.markForPosition(book.content.positionInChapter)
    val positionInCurrentMark = if (isPlaying && currentMark.durationMs > 0) {
      val relativePosition = book.content.positionInChapter - currentMark.startMs
      relativePosition.coerceIn(0L, currentMark.durationMs)
    } else {
      book.content.positionInChapter - currentMark.startMs
    }
    val currentMarkDuration = currentMark.durationMs

    val sleepTime = remember { sleepTimer.state }.collectAsState().value
    val sleepTimerActive = sleepTime.enabled
    val sleepTimerRemaining = when (sleepTime) {
      is SleepTimerState.Enabled.WithDuration -> sleepTime.leftDuration
      else -> null
    }

    val hasMoreThanOneChapter = book.chapters.sumOf { it.chapterMarks.count() } > 1

    val chapterItems = buildList {
      book.chapters.forEach { chapter ->
        chapter.chapterMarks.forEach { chapterMark ->
          val isCurrent = chapterMark == book.currentMark && chapter == book.currentChapter
          add(
            BookPlayViewState.ChapterItem(
              name = chapterMark.name ?: "第${size + 1}集",
              duration = formatTime(chapterMark.durationMs),
              playedTime = if (isCurrent) positionInCurrentMark else 0L,
            )
          )
        }
      }
    }
    val currentChapterFlatIndex = book.chapters.takeWhile { it != book.currentChapter }
      .sumOf { it.chapterMarks.count() } + book.currentChapter.chapterMarks.indexOf(book.currentMark)

    // 最后一集检测：如果当前是最后一集且定时开启，播放完成后自动关闭定时
    val totalChapters = book.chapters.sumOf { it.chapterMarks.count() }
    val isLastChapter = currentChapterFlatIndex >= totalChapters - 1
    LaunchedEffect(isLastChapter, isPlaying) {
      if (isLastChapter && !isPlaying && (sleepTimerActive || episodeCountdown > 0)) {
        // 最后一集播放完成后自动关闭所有定时
        sleepTimer.disable()
        episodeCountdown = 0
        lastChapterIndex = -1
      }
    }

    return BookPlayViewState(
      sleepTimerActive = sleepTimerActive,
      sleepTimerRemaining = sleepTimerRemaining,
      episodeCountdown = episodeCountdown,
      playing = isPlaying,
      title = book.content.name,
      showPreviousNextButtons = hasMoreThanOneChapter,
      chapterName = currentMark.name.takeIf { hasMoreThanOneChapter },
      duration = currentMarkDuration.milliseconds,
      playedTime = positionInCurrentMark.coerceAtMost(currentMarkDuration).milliseconds,
      cover = book.content.coverUrl,
      skipSilence = skipSilence,
      chapters = chapterItems,
      currentChapterIndex = currentChapterFlatIndex,
      timerActive = sleepTimerActive || episodeCountdown > 0,
    )
  }

  private fun kioskModeViewState(): BookPlayViewState {
    val currentlyPlaying = KioskModeDemoData.currentlyPlaying
    val book = KioskModeDemoData.currentlyPlayingBook
    return BookPlayViewState(
      sleepTimerActive = false,
      sleepTimerRemaining = null,
      episodeCountdown = 0,
      playing = true,
      title = currentlyPlaying.title,
      showPreviousNextButtons = true,
      chapterName = currentlyPlaying.chapter,
      duration = 14.hours + 27.minutes,
      playedTime = 10.hours + 24.minutes,
      cover = book.coverUrl,
      skipSilence = false,
      chapters = listOf(
        BookPlayViewState.ChapterItem("第一章", "1:30:00", 0L),
        BookPlayViewState.ChapterItem("第二章", "2:00:00", 0L),
      ),
      currentChapterIndex = 0,
      timerActive = false,
    )
  }

  fun dismissDialog() {
    Logger.d("dismissDialog")
    dialogState.value = null
  }

  fun onAcceptSleepTimer(minutes: Int) {
    scope.launch {
      val book = currentBook() ?: return@launch
      // 如果定集已开启，先关闭
      if (episodeCountdown > 0) {
        episodeCountdown = 0
        lastChapterIndex = -1
      }
      bookmarkRepository.addBookmarkAtBookPosition(
        book = book,
        setBySleepTimer = true,
        title = null,
      )
      sleepTimer.enable(SleepTimerMode.TimedWithDuration(minutes.minutes))
      showSleepTimerDialog.value = false
    }
  }

  fun onAcceptSleepEpisodeCount(count: Int) {
    scope.launch {
      val book = currentBook() ?: return@launch
      // 如果定时已开启，先关闭
      if (sleepTimer.state.value.enabled) {
        sleepTimer.disable()
      }
      // 获取实时播放位置（优先使用播放器实时位置，而非持久化位置）
      val liveState = player.livePlaybackState(bookId)
      val currentPositionInChapter = liveState?.positionMs ?: book.content.positionInChapter

      val currentChapterIndex = book.content.currentChapterIndex
      val skipStart = skipStartSecondsStore.data.first()
      val skipEnd = skipEndSecondsStore.data.first()
      val totalDurationMs = calculateEpisodeCountdownDurationMs(
        chapters = book.chapters,
        currentChapterIndex = currentChapterIndex,
        currentPositionInChapterMs = currentPositionInChapter,
        count = count,
        skipStartSeconds = skipStart,
        skipEndSeconds = skipEnd,
      )

      val totalDuration = (totalDurationMs.coerceAtLeast(0L) + 1000L).milliseconds
      sleepTimer.enable(SleepTimerMode.TimedWithDuration(totalDuration))
      episodeCountdown = count
      showSleepTimerDialog.value = false
    }
  }

  fun onPlaybackSpeedChanged(speed: Float) {
    dialogState.value = BookPlayDialogViewState.SpeedDialog(speed)
    player.setSpeed(speed)
  }

  fun onVolumeGainChanged(gain: Decibel) {
    dialogState.value = volumeGainDialogViewState(gain)
    player.setGain(gain)
  }

  fun next() {
    player.next()
  }

  fun previous() {
    player.previous()
  }

  fun playPause() {
    if (playStateManager.playState != PlayStateManager.PlayState.Playing) {
      scope.launch {
        if (batteryOptimization.shouldRequest()) {
          viewEffects.tryEmit(BookPlayViewEffect.RequestIgnoreBatteryOptimization)
          batteryOptimization.onBatteryOptimizationsRequested()
        }
      }
    }
    player.playPause()
  }

  fun rewind() {
    player.rewind()
  }

  fun fastForward() {
    player.fastForward()
  }

  fun onCloseClick() {
    navigator.goBack()
  }

  fun onSettingsClick() {
    navigator.goTo(Destination.Settings)
  }

  fun onCurrentChapterClick() {
    scope.launch {
      val book = currentBook() ?: return@launch
      dialogState.value = BookPlayDialogViewState.SelectChapterDialog(
        items = book.chapters.flatMapIndexed { chapterIndex, chapter ->
          chapter.chapterMarks.mapIndexed { markIndex, chapterMark ->
            val previousChapters = book.chapters.take(chapterIndex)
            BookPlayDialogViewState.SelectChapterDialog.ItemViewState(
              number = previousChapters.sumOf { it.chapterMarks.count() } + markIndex + 1,
              name = chapterMark.name ?: "",
              active = chapterMark == book.currentMark && chapter == book.currentChapter,
              time = formatTime(previousChapters.sumOf { it.duration } + chapterMark.startMs),
            )
          }
        },
      )
    }
  }

  fun onChapterClick(number: Int) {
    scope.launch {
      val book = currentBook() ?: return@launch
      var currentIndex = -1
      book.chapters.forEach { chapter ->
        chapter.chapterMarks.forEach { mark ->
          currentIndex++
          if (currentIndex == number - 1) {
            player.setPosition(mark.startMs, chapter.id)
            dialogState.value = null
            return@launch
          }
        }
      }
    }
  }

  fun onPlaybackSpeedIconClick() {
    scope.launch {
      val playbackSpeed = currentBook()?.content?.playbackSpeed ?: return@launch
      dialogState.value = BookPlayDialogViewState.SpeedDialog(playbackSpeed)
    }
  }

  fun onVolumeGainIconClick() {
    scope.launch {
      val content = currentBook()?.content ?: return@launch
      dialogState.value = volumeGainDialogViewState(Decibel(content.gain))
    }
  }

  private fun volumeGainDialogViewState(gain: Decibel): BookPlayDialogViewState.VolumeGainDialog {
    return BookPlayDialogViewState.VolumeGainDialog(
      gain = gain,
      maxGain = VolumeGain.MAX_GAIN,
      valueFormatted = volumeGainFormatter.format(gain),
    )
  }

  fun onBookmarkClick() {
    navigator.goTo(Destination.Bookmarks(bookId))
  }

  fun onBookmarkLongClick() {
    scope.launch {
      val book = currentBook() ?: return@launch
      bookmarkRepository.addBookmarkAtBookPosition(
        book = book,
        title = null,
        setBySleepTimer = false,
      )
      viewEffects.tryEmit(BookPlayViewEffect.BookmarkAdded)
    }
  }

  fun seekTo(position: Duration) {
    scope.launch {
      val book = currentBook() ?: return@launch
      val currentChapter = book.currentChapter
      val currentMark = currentChapter.markForPosition(book.content.positionInChapter)
      player.setPosition(currentMark.startMs + position.inWholeMilliseconds, currentChapter.id)
    }
  }

  fun toggleSleepTimer() {
    scope.launch {
      val timerEnabled = sleepTimer.state.value.enabled || episodeCountdown > 0
      if (timerEnabled) {
        // 关闭所有定时功能
        sleepTimer.disable()
        episodeCountdown = 0
        lastChapterIndex = -1
        showSleepTimerDialog.value = false
      } else {
        showSleepTimerDialog.value = true
      }
    }
  }

  fun onBatteryOptimizationRequested() {
    navigator.goTo(Destination.BatteryOptimization)
  }

  private fun calculateEpisodeCountdownDurationMs(
    chapters: List<Chapter>,
    currentChapterIndex: Int,
    currentPositionInChapterMs: Long,
    count: Int,
    skipStartSeconds: Int,
    skipEndSeconds: Int,
  ): Long {
    if (count <= 0 || currentChapterIndex !in chapters.indices) return 0L

    val currentChapter = chapters[currentChapterIndex]
    val currentChapterRemainingMs = if (skipStartSeconds > 0 || skipEndSeconds > 0) {
      ChapterDurationHelper.remainingEffectiveDuration(
        chapter = currentChapter,
        positionInChapterMs = currentPositionInChapterMs,
        skipStartSeconds = skipStartSeconds,
        skipEndSeconds = skipEndSeconds,
      )
    } else {
      (currentChapter.duration - currentPositionInChapterMs).coerceAtLeast(0L)
    }

    var totalDurationMs = currentChapterRemainingMs
    for (offset in 1 until count) {
      val chapterIndex = currentChapterIndex + offset
      if (chapterIndex >= chapters.size) break
      val chapter = chapters[chapterIndex]
      totalDurationMs += if (skipStartSeconds > 0 || skipEndSeconds > 0) {
        ChapterDurationHelper.effectiveDuration(
          chapter = chapter,
          skipStartSeconds = skipStartSeconds,
          skipEndSeconds = skipEndSeconds,
        )
      } else {
        chapter.duration
      }
    }
    return totalDurationMs
  }

  private suspend fun currentBook(): Book? {
    return currentBookResolver.book(bookId)
  }

  @AssistedFactory
  interface Factory {
    fun create(bookId: BookId): BookPlayViewModel
  }
}
