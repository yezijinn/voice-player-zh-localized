package voice.features.settings

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.launch
import voice.core.common.AppInfoProvider
import voice.core.common.DispatcherProvider
import voice.core.common.MainScope
import voice.core.data.ThemeColorScheme
import voice.core.data.ThemeMode
import voice.core.data.sleeptimer.SleepTimerPreference
import voice.core.data.store.AnalyticsConsentStore
import voice.core.data.store.DeveloperMenuUnlockedStore
import voice.core.data.store.SkipEndSecondsStore
import voice.core.data.store.SkipSilenceStore
import voice.core.data.store.SkipStartSecondsStore
import voice.core.data.store.SleepTimerPreferenceStore
import voice.core.data.store.ThemeColorSchemeStore
import voice.core.data.store.ThemeModeStore
import voice.core.featureflag.FeatureFlag
import voice.core.featureflag.KioskModeFeatureFlagQualifier
import voice.core.ui.DynamicColorAvailability
import voice.navigation.Destination
import voice.navigation.Navigator
import java.time.LocalTime
import kotlin.time.Duration.Companion.minutes

@Inject
class SettingsViewModel(
  @ThemeModeStore
  private val themeModeStore: DataStore<ThemeMode>,
  @ThemeColorSchemeStore
  private val themeColorSchemeStore: DataStore<ThemeColorScheme>,
  @SkipStartSecondsStore
  private val skipStartSecondsStore: DataStore<Int>,
  @SkipEndSecondsStore
  private val skipEndSecondsStore: DataStore<Int>,
  @SkipSilenceStore
  private val skipSilenceStore: DataStore<Boolean>,
  private val navigator: Navigator,
  private val appInfoProvider: AppInfoProvider,
  @SleepTimerPreferenceStore
  private val sleepTimerPreferenceStore: DataStore<SleepTimerPreference>,
  @AnalyticsConsentStore
  private val analyticsConsentStore: DataStore<Boolean>,
  @KioskModeFeatureFlagQualifier
  private val kioskModeFeatureFlag: FeatureFlag<Boolean>,
  @DeveloperMenuUnlockedStore
  private val developerMenuUnlockedStore: DataStore<Boolean>,
  private val dynamicColorAvailability: DynamicColorAvailability,
  dispatcherProvider: DispatcherProvider,
) : SettingsListener {

  private val mainScope = MainScope(dispatcherProvider)
  private val dialog = mutableStateOf<SettingsViewState.Dialog?>(null)

  @Composable
  fun viewState(): SettingsViewState {
    val themeMode by remember { themeModeStore.data }.collectAsState(initial = ThemeMode.Light)
    val themeColorScheme by remember { themeColorSchemeStore.data }.collectAsState(initial = ThemeColorScheme.Teal)
    val skipStartSeconds by remember { skipStartSecondsStore.data }.collectAsState(initial = 0)
    val skipEndSeconds by remember { skipEndSecondsStore.data }.collectAsState(initial = 0)
    val skipSilence by remember { skipSilenceStore.data }.collectAsState(initial = false)
    val autoSleepTimer by remember { sleepTimerPreferenceStore.data }.collectAsState(
      initial = SleepTimerPreference.Default,
    )
    val analyticsEnabled by remember { analyticsConsentStore.data }.collectAsState(initial = false)
    val kioskMode = remember {
      kioskModeFeatureFlag.get()
    }
    val showDeveloperMenu by remember { developerMenuUnlockedStore.data }.collectAsState(initial = false)
    val showThemeColorSchemePref = remember { true }
    return SettingsViewState(
      themeMode = themeMode,
      themeColorScheme = themeColorScheme,
      showThemeColorSchemePref = showThemeColorSchemePref,
      skipStartSeconds = skipStartSeconds,
      skipEndSeconds = skipEndSeconds,
      skipSilence = skipSilence,
      dialog = dialog.value,
      appVersion = appInfoProvider.versionName,
      buildTimestamp = appInfoProvider.buildTimestamp,
      autoSleepTimer = SettingsViewState.AutoSleepTimerViewState(
        enabled = autoSleepTimer.autoSleepTimerEnabled,
        startTime = autoSleepTimer.autoSleepStartTime,
        endTime = autoSleepTimer.autoSleepEndTime,
        durationMinutes = autoSleepTimer.duration.inWholeMinutes.toInt(),
        endOfTimerKillApp = autoSleepTimer.endOfTimerKillApp,
      ),
      analyticsEnabled = analyticsEnabled,
      showAnalyticSetting = appInfoProvider.analyticsIncluded,
      showDeveloperMenu = showDeveloperMenu,
      showSupportDevelopment = appInfoProvider.supportDevelopmentIncluded,
      kioskMode = kioskMode,
    )
  }

  override fun close() {
    navigator.goBack()
  }

  override fun onThemeModeRowClick() {
    dialog.value = SettingsViewState.Dialog.Theme
  }

  override fun onThemeColorSchemeRowClick() {
    dialog.value = SettingsViewState.Dialog.ColorScheme
  }

  override fun setThemeMode(themeMode: ThemeMode) {
    mainScope.launch {
      themeModeStore.updateData { themeMode }
    }
    dialog.value = null
  }

  override fun setThemeColorScheme(themeColorScheme: ThemeColorScheme) {
    mainScope.launch {
      themeColorSchemeStore.updateData { themeColorScheme }
    }
    dialog.value = null
  }

  override fun skipStartSecondsChanged(seconds: Int) {
    mainScope.launch {
      skipStartSecondsStore.updateData { seconds }
    }
  }

  override fun onSkipStartSecondsRowClick() {
    dialog.value = SettingsViewState.Dialog.SkipStartSeconds
  }

  override fun skipEndSecondsChanged(seconds: Int) {
    mainScope.launch {
      skipEndSecondsStore.updateData { seconds }
    }
  }

  override fun onSkipEndSecondsRowClick() {
    dialog.value = SettingsViewState.Dialog.SkipEndSeconds
  }

  override fun dismissDialog() {
    dialog.value = null
  }

  override fun toggleSkipSilence() {
    mainScope.launch {
      skipSilenceStore.updateData { !it }
    }
  }

  override fun getSupport() {
    navigator.goTo(Destination.Website("https://github.com/PaulWoitaschek/Voice/discussions/categories/q-a"))
  }

  override fun suggestIdea() {
    navigator.goTo(Destination.Website("https://github.com/PaulWoitaschek/Voice/discussions/categories/ideas"))
  }

  override fun openBugReport() {
    val url = "https://github.com/PaulWoitaschek/Voice/issues/new".toUri()
      .buildUpon()
      .appendQueryParameter("template", "bug.yml")
      .appendQueryParameter("version", appInfoProvider.versionName)
      .appendQueryParameter("androidversion", Build.VERSION.SDK_INT.toString())
      .appendQueryParameter("device", Build.MODEL)
      .toString()
    navigator.goTo(Destination.Website(url))
  }

  override fun openTranslations() {
    dismissDialog()
    navigator.goTo(Destination.Website("https://hosted.weblate.org/engage/voice/"))
  }

  override fun openFaq() {
    navigator.goTo(Destination.Website("https://voice.woitaschek.de/faq/"))
  }

  override fun openSupportVoice() {
    navigator.goTo(Destination.SupportVoice)
  }

  override fun openFolderPicker() {
    navigator.goTo(Destination.FolderPicker)
  }

  override fun setAutoSleepTimer(checked: Boolean) {
    mainScope.launch {
      sleepTimerPreferenceStore.updateData { currentPrefs ->
        currentPrefs.copy(autoSleepTimerEnabled = checked)
      }
    }
  }

  override fun setAutoSleepTimerStart(time: LocalTime) {
    mainScope.launch {
      sleepTimerPreferenceStore.updateData { currentPrefs ->
        currentPrefs.copy(autoSleepStartTime = time)
      }
    }
  }

  override fun setAutoSleepTimerEnd(time: LocalTime) {
    mainScope.launch {
      sleepTimerPreferenceStore.updateData { currentPrefs ->
        currentPrefs.copy(autoSleepEndTime = time)
      }
    }
  }

  override fun setAutoSleepTimerDuration(durationMinutes: Int) {
    mainScope.launch {
      sleepTimerPreferenceStore.updateData { currentPrefs ->
        currentPrefs.copyWithDuration(durationMinutes.minutes)
      }
    }
  }

  override fun toggleAnalytics() {
    mainScope.launch {
      analyticsConsentStore.updateData { !it }
    }
  }

  override fun openDeveloperMenu() {
    navigator.goTo(Destination.DeveloperSettings)
  }

  override fun setEndOfTimerKillApp(enabled: Boolean) {
    mainScope.launch {
      sleepTimerPreferenceStore.updateData { currentPrefs ->
        currentPrefs.copy(endOfTimerKillApp = enabled)
      }
    }
  }
}