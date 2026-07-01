package voice.core.data.sleeptimer

import kotlinx.serialization.Serializable
import voice.core.common.serialization.LocalTimeSerializer
import java.time.LocalTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@Serializable
public data class SleepTimerPreference(
  /**
   * The custom sleep time duration
   */
  val duration: Duration,
  /**
   * If the sleep timer should be automatically enabled between [autoSleepStartTime] and [autoSleepEndTime]
   */
  val autoSleepTimerEnabled: Boolean,
  @Serializable(with = LocalTimeSerializer::class)
  val autoSleepStartTime: LocalTime,
  @Serializable(with = LocalTimeSerializer::class)
  val autoSleepEndTime: LocalTime,
  /**
   * 定时结束时是否关闭播放器（结束APP进程）
   */
  val endOfTimerKillApp: Boolean = false,
) {

  public companion object {
    public val Default: SleepTimerPreference = SleepTimerPreference(
      autoSleepTimerEnabled = false,
      autoSleepStartTime = LocalTime.of(22, 0),
      autoSleepEndTime = LocalTime.of(6, 0),
      duration = 20.minutes,
      endOfTimerKillApp = false,
    )
  }

  public fun copyWithDuration(duration: Duration): SleepTimerPreference {
    return copy(duration = duration)
  }
}
