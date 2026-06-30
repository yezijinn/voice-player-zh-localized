# 有声书播放器 - UI 自定义修改指南

> 本指南帮助您快速定位和修改 APP 中的 UI 文字显示。

---

## 一、修改各国语言字符串（推荐）

所有用户可见的文字都定义在字符串资源文件中，修改后会自动生效。

### 中文（简体）
**文件路径：**
```
Voice-main\core\strings\src\main\res\values-zh-rCN\strings.xml
```

### 英文
**文件路径：**
```
Voice-main\core\strings\src\main\res\values\strings.xml
```

### 修改示例

打开 `strings.xml`，找到对应的 `<string name="xxx">文字</string>`，修改 `文字` 部分即可。

| 界面位置 | 字符串 name | 当前值 |
|---------|------------|--------|
| 搜索栏提示 | `library.search.hint` | 搜索 |
| 播放页左上角按钮 | `common.action.home` | 主页 |
| 播放页"定时"按钮 | `sleep_timer.action.open` | 定时 |
| 播放页"书签"按钮 | `bookmark.title` | 书签 |
| 播放页"速度"按钮 | `playback.speed.title` | 速度 |
| 播放页"设置"按钮 | `settings.action.open` | 设置 |
| 定时对话框标题 | `sleep_timer.dialog.title` | 播放一段时间后自动停止播放 |
| 定时"播完一集" | `sleep_timer.end_of_chapter` | 播完一集 |
| 设置页"跳过无声" | `playback.option.skip_silence` | 跳过无声的音频部分 |
| 设置页"跳过片头" | `playback.option.skip_start` | 跳过片头 |
| 设置页"跳过片尾" | `playback.option.skip_end` | 跳过片尾 |
| 主题"浅色" | `settings.appearance.theme.light` | 浅色 |
| 主题"深色" | `settings.appearance.theme.dark` | 深色 |
| 开源声明标题 | `settings.attribution.title` | 开源项目声明 |
| 二次开发信息 | `settings.developer_info.title` | 二次开发信息 |
| 欢迎页标题 | `onboarding.welcome.title` | 欢迎使用有声书播放器！ |
| 删除确认 | `book.delete.dialog.title` | 删除有声书 |
| 文件夹选择标题 | `folder.type.title` | 您的文件是如何组织的？ |

---

## 二、修改代码中的硬编码文字

部分文字直接在 Kotlin 代码中硬编码，需要修改源码文件。

### 1. 播放列表标题
**文件：** `Voice-main\features\playbackScreen\src\main\kotlin\voice\features\playbackScreen\view\PlaylistRow.kt`
```
第 49 行：Text(text = "播放列表", ...)
```
修改 `"播放列表"` 为需要的文字。

### 2. 主题配色名称
**文件：** `Voice-main\features\settings\src\main\kotlin\voice\features\settings\views\ThemeRows.kt`
```
第 197-203 行：ThemeMode.label()   — 主题模式名称
第 206-214 行：ThemeColorScheme.label() — 配色方案名称
```

### 3. 播放页顶部菜单按钮布局
**文件：** `Voice-main\features\playbackScreen\src\main\kotlin\voice\features\playbackScreen\view\BookPlayAppBar.kt`
```
第 37 行：horizontalArrangement = Arrangement.SpaceEvenly  — 菜单间距
可选值：SpaceEvenly（等距）、SpaceAround（环绕）、SpaceBetween（两端对齐）
```

### 4. 设置页"跳过无声"开关
**文件：** `Voice-main\features\settings\src\main\kotlin\voice\features\settings\views\Settings.kt`
```
第 202-219 行：SkipSilenceRow 组件 — 跳过无声开关的 UI
```

### 5. 定时对话框
**文件：** `Voice-main\features\sleepTimer\src\main\kotlin\voice\features\sleepTimer\SleepTimerDialog.kt`
```
第 63 行：自定义时长输入框标签 "自定义时长（分钟）"
第 75 行：确定按钮文字 "确定"
```

---

## 三、修改包名和应用名

### 包名
**文件：** `Voice-main\app\build.gradle.kts`
```
第 35 行：applicationId = "com.jinn.Voice"
```

### 应用名（桌面显示名称）
**文件：** `Voice-main\app\src\main\res\values\strings.xml`（查找 `app_name`）
```
<string name="app_name">有声书播放器</string>
```

---

## 四、修改配色方案

### 默认配色
**文件：** `Voice-main\features\settings\src\main\kotlin\voice\features\settings\SettingsViewModel.kt`
```
第 75 行：initial = ThemeColorScheme.Teal  — 默认配色
```

### 配色方案定义
**文件：** `Voice-main\core\data\api\src\main\kotlin\voice\core\data\ThemeColorScheme.kt`
```
enum class ThemeColorScheme — 可用的配色方案枚举
```

### 配色具体颜色
**文件：** `Voice-main\core\ui\src\main\kotlin\voice\core\ui\VoiceTheme.kt`
```
各配色方案的具体颜色定义
```

---

## 五、修改默认主题

**文件：** `Voice-main\features\settings\src\main\kotlin\voice\features\settings\SettingsViewModel.kt`
```
第 74 行：initial = ThemeMode.Light  — 默认主题
可选值：ThemeMode.Light（浅色）、ThemeMode.Dark（深色）、ThemeMode.FollowSystem（跟随系统）
```

---

## 六、修改版本号

**文件：** `Voice-main\app\build.gradle.kts`
```
第 36 行：versionCode = 20260629
第 37 行：versionName = "20260629"
```

---

## 七、一键打包

修改完代码后，运行以下命令一键打包：

```powershell
.\一键打包.bat
```

或手动执行：

```powershell
$env:JAVA_HOME = 'C:\Android\jdk25\jdk-25.0.3+9'
$env:JAVA_TOOL_OPTIONS = '-Djava.io.tmpdir=C:/temp/kotlin-temp'
$env:KOTLIN_TEMP_DIR = 'C:\temp\kotlin-temp'
cd C:\temp\Voice6
.\gradlew.bat assembleFreeDebug --no-daemon --no-build-cache
```

APK 输出位置：`C:\temp\Voice6\app\build\outputs\apk\free\debug\app-free-debug.apk`

---

## 注意事项

1. 修改 `strings.xml` 后直接生效，无需重新编译 Kotlin
2. 修改 `.kt` 文件后必须重新编译
3. 字符串资源文件中的 `%1$s`、`%d` 等是占位符，不要删除
4. 修改后请运行打包脚本验证 APK 是否正常生成