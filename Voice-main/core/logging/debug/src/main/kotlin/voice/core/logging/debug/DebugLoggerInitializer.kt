package voice.core.logging.debug

import android.app.Application
import androidx.datastore.core.DataStore
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import voice.core.data.store.DebugLogStore
import voice.core.initializer.AppInitializer
import voice.core.logging.DebugLogger

@ContributesIntoSet(AppScope::class)
class DebugLoggerInitializer : AppInitializer {

  @Inject
  @DebugLogStore
  lateinit var debugLogStore: DataStore<Boolean>

  override fun onAppStart(application: Application) {
    DebugLogger.init(application, debugLogStore)
  }
}