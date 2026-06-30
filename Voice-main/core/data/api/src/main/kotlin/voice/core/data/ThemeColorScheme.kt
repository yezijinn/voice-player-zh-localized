package voice.core.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public enum class ThemeColorScheme {
  @SerialName("Red")
  Red,

  @SerialName("Green")
  Green,

  @SerialName("Purple")
  Purple,

  @SerialName("Teal")
  Teal,

  @SerialName("Orange")
  Orange,

  @SerialName("Dynamic")
  Dynamic,
}
