# 自定义修改UI指南

本文档指导开发者如何自定义修改有声书播放器的UI界面。

## 目录

- [修改应用名称](#修改应用名称)
- [修改应用图标](#修改应用图标)
- [修改主题颜色](#修改主题颜色)
- [修改播放界面](#修改播放界面)
- [修改字体样式](#修改字体样式)

## 修改应用名称

应用名称定义在以下文件中：

```
core/ui/src/main/res/values/donottranslate.xml
core/ui/src/debug/res/values/donottranslate.xml
core/common/src/debug/res/values/donottranslate.xml
```

修改 `<string name="app_name">` 的值即可。

## 修改应用图标

应用图标位于：

```
app/src/main/res/mipmap-*/
core/ui/src/main/res/drawable/
```

替换对应的图片文件即可。

## 修改主题颜色

主题颜色定义在：

```
core/ui/src/main/kotlin/voice/core/ui/theme/Theme.kt
```

修改 `ColorScheme` 中的颜色值。

## 修改播放界面

播放界面相关文件：

```
features/playbackScreen/src/main/kotlin/voice/features/playbackScreen/view/
├── BookPlayView.kt          # 主播放界面
├── BookPlayAppBar.kt        # 顶部工具栏
├── BookPlayContent.kt       # 内容区域
├── CoverRow.kt              # 封面区域
├── PlaylistRow.kt           # 播放列表
├── CurrentChapter.kt        # 当前章节信息
├── PlayButtons.kt           # 播放控制按钮
├── SleepTimerButton.kt      # 定时按钮
└── BookPlaySlider.kt        # 进度条
```

### 修改按钮样式

编辑 `PlayButtons.kt`：

```kotlin
IconButton(
  modifier = Modifier.size(64.dp),  // 修改按钮大小
  onClick = onPlayClick,
) {
  Icon(
    imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
    contentDescription = if (playing) "暂停" else "播放",
    modifier = Modifier.fillMaxSize(),
    tint = MaterialTheme.colorScheme.primary,  // 修改颜色
  )
}
```

### 修改进度条样式

编辑 `BookPlaySlider.kt`：

```kotlin
Slider(
  value = position,
  onValueChange = onValueChange,
  colors = SliderDefaults.colors(
    thumbColor = Color.Red,        // 滑块颜色
    activeTrackColor = Color.Blue, // 已播放部分颜色
    inactiveTrackColor = Color.Gray // 未播放部分颜色
  )
)
```

## 修改字体样式

字体样式定义在：

```
core/ui/src/main/kotlin/voice/core/ui/theme/Type.kt
```

修改 `Typography` 中的字体配置。

## 常用修改示例

### 修改播放按钮大小

```kotlin
// PlayButtons.kt
IconButton(
  modifier = Modifier.size(80.dp),  // 默认64.dp，改为80.dp
  onClick = onPlayClick,
)
```

### 修改封面圆角

```kotlin
// CoverRow.kt
Image(
  painter = painter,
  contentDescription = null,
  modifier = Modifier
    .fillMaxWidth()
    .aspectRatio(1f)
    .clip(RoundedCornerShape(24.dp)),  // 修改圆角大小
)
```

### 修改定时按钮文字

```kotlin
// SleepTimerButton.kt
Text(
  text = if (timerActive) "定时:开" else "定时:关",
  style = MaterialTheme.typography.labelMedium,
)
```

## 注意事项

1. 修改后需要重新编译才能生效
2. 建议先备份原始文件
3. 遵循 Material Design 3 设计规范
4. 保持代码风格一致
