package voice.core.sleeptimer

import androidx.datastore.core.DataStore
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import voice.core.common.DispatcherProvider
import voice.core.common.MainScope
import voice.core.data.sleeptimer.SleepTimerPreference
import voice.core.data.store.FadeOutStore
import voice.core.data.store.SleepTimerPreferenceStore
import voice.core.logging.api.Logger
import voice.core.playback.PlayerController
import voice.core.playback.playstate.PlayStateManager
import voice.core.playback.playstate.PlayStateManager.PlayState.Playing
import kotlin.math.max
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class SleepTimerImpl internal constructor(
  private val playStateManager: PlayStateManager,
  private val shakeDetector: ShakeDetector,
  @SleepTimerPreferenceStore
  private val sleepTimerPreferenceStore: DataStore<SleepTimerPreference>,
  private val playerController: PlayerController,
  @FadeOutStore
  private val fadeOutStore: DataStore<Duration>,
  dispatcherProvider: DispatcherProvider,
  private val tracker: SleepTimerTracker,
) : SleepTimer {

  private val scope = MainScope(dispatcherProvider)
  override val state: StateFlow<SleepTimerState>
    field = MutableStateFlow<SleepTimerState>(SleepTimerState.Disabled)

  private var job: Job? = null

  override fun enable(mode: SleepTimerMode) {
    tracker.enabled(mode)
    disable() // cancel any active job first

    job = scope.launch {
      when (mode) {
        is SleepTimerMode.TimedWithDuration -> startCountdown(mode.duration)
        SleepTimerMode.TimedWithDefault -> {
          val pref = sleepTimerPreferenceStore.data.first()
          startCountdown(pref.duration)
        }
      }
    }
  }

  override fun disable() {
    tracker.disabled()
    job?.cancel()
    job = null
    state.value = SleepTimerState.Disabled
    playerController.setVolume(1F)
  }

  private tailrec suspend fun startCountdown(duration: Duration) {
    Logger.d("startCountdown(duration=$duration)")
    var left = duration
    state.value = SleepTimerState.Enabled.WithDuration(left)
    playerController.setVolume(1F)

    val fadeOutDuration = fadeOutStore.data.first()
    val calibrationInterval = 10.seconds  // 每10秒检查一次用户设置
    var interval = 500.milliseconds

    while (left > Duration.ZERO) {
      suspendUntilPlaying()
      if (left < fadeOutDuration) {
        interval = 200.milliseconds
        updateVolume(left, fadeOutDuration)
      }

      // 每隔 calibrationInterval 秒检查用户是否手动关闭了功能
      if (left <= calibrationInterval || (duration.inWholeMilliseconds - left.inWholeMilliseconds) % calibrationInterval.inWholeMilliseconds < interval.inWholeMilliseconds) {
        val currentPref = sleepTimerPreferenceStore.data.first()
        Logger.d("Periodic check: endOfTimerKillApp = ${currentPref.endOfTimerKillApp}")
      }

      delay(interval)
      left = max((left - interval).inWholeMilliseconds, 0).milliseconds
      state.value = SleepTimerState.Enabled.WithDuration(left)
    }
    playerController.setVolume(1f)
    state.value = SleepTimerState.Disabled

    // 先暂停播放
    playerController.pauseWithRewind(fadeOutDuration)

    // 定时结束，实时检查开关状态（如果用户手动关闭了功能，不再触发）
    val finalPref = sleepTimerPreferenceStore.data.first()
    if (finalPref.endOfTimerKillApp) {
      // 延迟3秒后关闭APP
      delay(3.seconds)
      killAppProcess()
    }

    val shakeDetected = detectShakeWithTimeout()
    playerController.setVolume(1F)
    if (shakeDetected) {
      Logger.i("Shake detected, resetting timer")
      playerController.play()
      startCountdown(duration)
    }
  }

  private suspend fun detectShakeWithTimeout(): Boolean {
    Logger.d("Waiting $SHAKE_TO_RESET_TIME for shake...")
    return withTimeoutOrNull(SHAKE_TO_RESET_TIME) {
      shakeDetector.detect()
      true
    } ?: false
  }

  private fun updateVolume(
    left: Duration,
    fadeOutDuration: Duration,
  ) {
    val percentage = (left / fadeOutDuration).toFloat().coerceIn(0f, 1f)
    val volume = 1 - FastOutSlowInInterpolator().getInterpolation(1 - percentage)
    playerController.setVolume(volume)
  }

  private suspend fun suspendUntilPlaying() {
    if (playStateManager.playState != Playing) {
      Logger.i("Not playing. Waiting for playback to continue.")
      playStateManager.playStateFlow.first { it == Playing }
      Logger.i("Playback resumed.")
    }
  }

  internal companion object {
    val SHAKE_TO_RESET_TIME = 30.seconds
  }

  private fun killAppProcess() {
    try {
      Logger.i("Killing app process as end of timer action")
      android.os.Process.killProcess(android.os.Process.myPid())
      System.exit(0)
    } catch (e: Exception) {
      Logger.e("Failed to kill app process: ${e.message}")
    }
  }
}
