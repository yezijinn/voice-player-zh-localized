package voice.features.settings

internal sealed interface SettingsViewEffect {
  data object DeveloperMenuUnlocked : SettingsViewEffect
  data class OpenGitHub(val url: String) : SettingsViewEffect
}