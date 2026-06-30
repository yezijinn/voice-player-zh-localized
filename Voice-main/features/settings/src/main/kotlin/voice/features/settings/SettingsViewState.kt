package voice.features.settings

import voice.core.data.ThemeColorScheme
import voice.core.data.ThemeMode
import java.time.LocalTime

data class SettingsViewState(
  val themeMode: ThemeMode,
  val themeColorScheme: ThemeColorScheme,
  val showThemeColorSchemePref: Boolean,
  val skipStartSeconds: Int,
  val skipEndSeconds: Int,
  val skipSilence: Boolean,
  val appVersion: String,
  val buildTimestamp: String,
  val dialog: Dialog?,
  val autoSleepTimer: AutoSleepTimerViewState,
  val showAnalyticSetting: Boolean,
  val analyticsEnabled: Boolean,
  val showDeveloperMenu: Boolean,
  val showSupportDevelopment: Boolean,
  val kioskMode: Boolean,
) {

  enum class Dialog {
    SkipStartSeconds,
    SkipEndSeconds,
    Theme,
    ColorScheme,
  }

  companion object {
    fun preview(): SettingsViewState {
      return SettingsViewState(
        themeMode = ThemeMode.Light,
        themeColorScheme = ThemeColorScheme.Teal,
        showThemeColorSchemePref = true,
        skipStartSeconds = 20,
        skipEndSeconds = 10,
        skipSilence = false,
        dialog = null,
        appVersion = "20260629",
        buildTimestamp = "2026-06-29 16:00:00",
        autoSleepTimer = AutoSleepTimerViewState.preview(),
        analyticsEnabled = false,
        showAnalyticSetting = true,
        showDeveloperMenu = true,
        showSupportDevelopment = true,
        kioskMode = false,
      )
    }
  }

  data class AutoSleepTimerViewState(
    val enabled: Boolean,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val durationMinutes: Int,
  ) {
    companion object {
      fun preview(): AutoSleepTimerViewState {
        return AutoSleepTimerViewState(
          enabled = false,
          startTime = LocalTime.of(22, 0),
          endTime = LocalTime.of(6, 0),
          durationMinutes = 20,
        )
      }
    }
  }
}