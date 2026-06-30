package voice.features.settings.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.NavEntry
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import voice.core.common.rootGraphAs
import voice.core.ui.VoiceTheme
import voice.features.settings.SettingsListener
import voice.features.settings.SettingsViewEffect
import voice.features.settings.SettingsViewModel
import voice.features.settings.SettingsViewState
import voice.features.settings.views.sleeptimer.AutoSleepTimerCard
import voice.navigation.Destination
import voice.navigation.NavEntryProvider
import voice.core.strings.R as StringsR

@Composable
@Preview
private fun SettingsPreview() {
  VoiceTheme {
    Settings(
      SettingsViewState.preview(),
      SettingsListener.noop(),
    )
  }
}

@Composable
private fun Settings(
  viewState: SettingsViewState,
  listener: SettingsListener,
  snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
  val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
  Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    snackbarHost = {
      SnackbarHost(hostState = snackbarHostState)
    },
    topBar = {
      TopAppBar(
        scrollBehavior = scrollBehavior,
        title = {
          Text(stringResource(StringsR.string.settings_action_open))
        },
        navigationIcon = {
          TextButton(
            onClick = {
              listener.close()
            },
          ) {
            Text(stringResource(StringsR.string.common_action_close))
          }
        },
      )
    },
  ) { contentPadding ->
    LazyColumn(contentPadding = contentPadding) {
      if (viewState.showDeveloperMenu && !viewState.kioskMode) {
        item {
          DeveloperMenuItem(
            onClick = listener::openDeveloperMenu,
          )
        }
      }
      item {
        ListItem(
          modifier = Modifier.clickable { listener.openFolderPicker() },
          headlineContent = {
            Text(stringResource(StringsR.string.library_folders_title))
          },
          supportingContent = {
            Text(stringResource(StringsR.string.settings_library_folders_summary))
          },
        )
      }
      item {
        ThemeModeRow(viewState.themeMode, listener::onThemeModeRowClick)
      }
      if (viewState.showThemeColorSchemePref) {
        item {
          ThemeColorSchemeRow(viewState.themeColorScheme, listener::onThemeColorSchemeRowClick)
        }
      }
      if (viewState.showAnalyticSetting && !viewState.kioskMode) {
        item {
          AnalyticsRow(analyticsEnabled = viewState.analyticsEnabled, toggle = listener::toggleAnalytics)
        }
      }
      item {
        SkipSilenceRow(viewState.skipSilence) {
          listener.toggleSkipSilence()
        }
      }

      item {
        SkipStartSecondsRow(viewState.skipStartSeconds) {
          listener.onSkipStartSecondsRowClick()
        }
      }

      item {
        SkipEndSecondsRow(viewState.skipEndSeconds) {
          listener.onSkipEndSecondsRowClick()
        }
      }

      item {
        AutoSleepTimerCard(viewState.autoSleepTimer, listener)
      }

      item {
        AppVersion(
          appVersion = viewState.appVersion,
          onClick = listener::onAppVersionClick,
        )
      }
      item {
        ListItem(
          headlineContent = {
            Text(stringResource(StringsR.string.settings_developer_info_title))
          },
          supportingContent = {
            Text(stringResource(StringsR.string.settings_developer_info_summary, viewState.buildTimestamp))
          },
        )
      }
      item {
        ListItem(
          headlineContent = {
            Text(stringResource(StringsR.string.settings_attribution_title))
          },
          supportingContent = {
            Text(stringResource(StringsR.string.settings_attribution_summary))
          },
        )
      }
      if (viewState.kioskMode) {
        if (viewState.showAnalyticSetting) {
          item {
            AnalyticsRow(analyticsEnabled = viewState.analyticsEnabled, toggle = listener::toggleAnalytics)
          }
        }
        if (viewState.showDeveloperMenu) {
          item {
            DeveloperMenuItem(
              onClick = listener::openDeveloperMenu,
            )
          }
        }
      }
    }
    Dialog(viewState, listener)
  }
}

@Composable
private fun AnalyticsRow(
  analyticsEnabled: Boolean,
  toggle: () -> Unit,
) {
  ListItem(
    modifier = Modifier.clickable { toggle() },
    headlineContent = {
      Text(text = stringResource(StringsR.string.settings_analytics_consent_title))
    },
    supportingContent = {
      Text(text = stringResource(StringsR.string.settings_analytics_consent_description))
    },
    trailingContent = {
      Switch(
        checked = analyticsEnabled,
        onCheckedChange = { toggle() },
      )
    },
  )
}

@Composable
private fun SkipSilenceRow(
  skipSilence: Boolean,
  toggle: () -> Unit,
) {
  ListItem(
    modifier = Modifier.clickable { toggle() },
    headlineContent = {
      Text(text = stringResource(StringsR.string.playback_option_skip_silence))
    },
    trailingContent = {
      Switch(
        checked = skipSilence,
        onCheckedChange = { toggle() },
      )
    },
  )
}

@ContributesTo(AppScope::class)
interface SettingsGraph {
  val settingsViewModel: SettingsViewModel
}

@ContributesTo(AppScope::class)
interface SettingsProvider {

  @Provides
  @IntoSet
  fun settingsNavEntryProvider(): NavEntryProvider<*> = NavEntryProvider<Destination.Settings> { key ->
    NavEntry(key) {
      Settings()
    }
  }
}

@Composable
fun Settings() {
  val viewModel = retain<SettingsViewModel> { rootGraphAs<SettingsGraph>().settingsViewModel }
  val snackbarHostState = remember { SnackbarHostState() }
  val viewState = viewModel.viewState()
  val currentDeveloperMenuUnlockedMessage = rememberUpdatedState(stringResource(StringsR.string.settings_developer_menu_unlocked))
  LaunchedEffect(viewModel) {
    viewModel.viewEffects.collect { viewEffect ->
      when (viewEffect) {
        SettingsViewEffect.DeveloperMenuUnlocked -> {
          snackbarHostState.showSnackbar(currentDeveloperMenuUnlockedMessage.value)
        }
      }
    }
  }
  Settings(viewState, viewModel, snackbarHostState)
}

@Composable
private fun Dialog(
  viewState: SettingsViewState,
  listener: SettingsListener,
) {
  val dialog = viewState.dialog ?: return
  when (dialog) {
    SettingsViewState.Dialog.SkipStartSeconds -> {
      SkipStartSecondsDialog(
        currentSeconds = viewState.skipStartSeconds,
        onSecondsConfirm = listener::skipStartSecondsChanged,
        onDismiss = listener::dismissDialog,
      )
    }
    SettingsViewState.Dialog.SkipEndSeconds -> {
      SkipEndSecondsDialog(
        currentSeconds = viewState.skipEndSeconds,
        onSecondsConfirm = listener::skipEndSecondsChanged,
        onDismiss = listener::dismissDialog,
      )
    }
    SettingsViewState.Dialog.Theme -> {
      ThemeModeDialog(
        selectedThemeMode = viewState.themeMode,
        onThemeModeSelect = listener::setThemeMode,
        onDismiss = listener::dismissDialog,
      )
    }
    SettingsViewState.Dialog.ColorScheme -> {
      ThemeColorSchemeDialog(
        selectedThemeColorScheme = viewState.themeColorScheme,
        onThemeColorSchemeSelect = listener::setThemeColorScheme,
        onDismiss = listener::dismissDialog,
      )
    }
  }
}
