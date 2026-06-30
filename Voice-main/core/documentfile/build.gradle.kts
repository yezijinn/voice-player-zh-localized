plugins {
  id("voice.library")
  alias(libs.plugins.metro)
}

dependencies {
  implementation(libs.androidxCore)
  implementation(projects.core.logging.api)
}
