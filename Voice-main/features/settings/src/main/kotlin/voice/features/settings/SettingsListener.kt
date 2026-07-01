package voice.features.settings

import voice.core.data.ThemeColorScheme
import voice.core.data.ThemeMode
import java.time.LocalTime

interface SettingsListener {
  fun close()
  fun onThemeModeRowClick()
  fun onThemeColorSchemeRowClick()
  fun setThemeMode(themeMode: ThemeMode)
  fun setThemeColorScheme(themeColorScheme: ThemeColorScheme)
  fun dismissDialog()
  fun getSupport()
  fun suggestIdea()
  fun openBugReport()
  fun openTranslations()
  fun openFaq()
  fun openSupportVoice()
  fun setAutoSleepTimer(checked: Boolean)
  fun setAutoSleepTimerStart(time: LocalTime)

  fun setAutoSleepTimerEnd(time: LocalTime)

  fun setAutoSleepTimerDuration(durationMinutes: Int)

  fun toggleAnalytics()
  fun openFolderPicker()
  fun onAppVersionClick()
  fun skipStartSecondsChanged(seconds: Int)
  fun onSkipStartSecondsRowClick()
  fun skipEndSecondsChanged(seconds: Int)
  fun onSkipEndSecondsRowClick()
  fun toggleSkipSilence()

  fun openDeveloperMenu()

  fun setEndOfTimerKillApp(enabled: Boolean)

  companion object {
    fun noop() = object : SettingsListener {
      override fun close() {}
      override fun onThemeModeRowClick() {}
      override fun onThemeColorSchemeRowClick() {}
      override fun setThemeMode(themeMode: ThemeMode) {}
      override fun setThemeColorScheme(themeColorScheme: ThemeColorScheme) {}
      override fun dismissDialog() {}
      override fun getSupport() {}
      override fun suggestIdea() {}
      override fun openBugReport() {}
      override fun openTranslations() {}
      override fun openFaq() {}
      override fun openSupportVoice() {}
      override fun setAutoSleepTimer(checked: Boolean) {}
      override fun setAutoSleepTimerStart(time: LocalTime) {}
      override fun setAutoSleepTimerEnd(time: LocalTime) {}
      override fun setAutoSleepTimerDuration(durationMinutes: Int) {}
      override fun toggleAnalytics() {}
      override fun openFolderPicker() {}
      override fun onAppVersionClick() {}
      override fun skipStartSecondsChanged(seconds: Int) {}
      override fun onSkipStartSecondsRowClick() {}
      override fun skipEndSecondsChanged(seconds: Int) {}
      override fun onSkipEndSecondsRowClick() {}
      override fun toggleSkipSilence() {}
      override fun openDeveloperMenu() {}
      override fun setEndOfTimerKillApp(enabled: Boolean) {}
    }
  }
}