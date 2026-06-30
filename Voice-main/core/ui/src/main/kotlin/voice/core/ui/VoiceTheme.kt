package voice.core.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.materialkolor.DynamicMaterialExpressiveTheme
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import voice.core.data.ThemeColorScheme
import voice.core.data.ThemeMode

val Red = Color(0xFFC62828)
val Green = Color(0xFF2E7D32)
val Purple = Color(0xFF6A1B9A)
val Teal = Color(0xFF00695C)
val Orange = Color(0xFFE65100)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun VoiceTheme(
  themeMode: ThemeMode = ThemeMode.FollowSystem,
  themeColorScheme: ThemeColorScheme = ThemeColorScheme.Red,
  content: @Composable () -> Unit,
) {
  val darkTheme = when (themeMode) {
    ThemeMode.FollowSystem -> isSystemInDarkTheme()
    ThemeMode.Light -> false
    ThemeMode.Dark -> true
  }
  val themedContent = remember(content) {
    movableContentOf {
      content()
    }
  }
  if (themeColorScheme == ThemeColorScheme.Dynamic && Build.VERSION.SDK_INT >= 31) {
    MaterialExpressiveTheme(
      colorScheme = systemDynamicColorScheme(darkTheme),
    ) {
      themedContent()
    }
  } else {
    val primaryColor = when (themeColorScheme) {
      ThemeColorScheme.Red -> Red
      ThemeColorScheme.Green -> Green
      ThemeColorScheme.Purple -> Purple
      ThemeColorScheme.Teal -> Teal
      ThemeColorScheme.Orange -> Orange
      ThemeColorScheme.Dynamic -> Red // fallback
    }
    DynamicMaterialExpressiveTheme(
      primary = primaryColor,
      secondary = primaryColor.copy(alpha = 0.7f),
      isDark = darkTheme,
      style = PaletteStyle.Expressive,
      specVersion = ColorSpec.SpecVersion.SPEC_2025,
    ) {
      themedContent()
    }
  }
}

@RequiresApi(31)
@Composable
private fun systemDynamicColorScheme(darkTheme: Boolean): ColorScheme {
  return if (darkTheme) {
    dynamicDarkColorScheme(LocalContext.current)
  } else {
    dynamicLightColorScheme(LocalContext.current)
  }
}
