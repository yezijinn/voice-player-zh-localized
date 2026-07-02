# Jinn有声书播放器

[![License](https://img.shields.io/badge/License-GPL--3.0-blue.svg)](LICENSE.md)
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Language](https://img.shields.io/badge/Language-Kotlin-purple.svg)]()
[![Latest Release](https://img.shields.io/github/v/release/yezijinn/voice-player-zh-localized)](https://github.com/yezijinn/voice-player-zh-localized/releases)
[![Last Commit](https://img.shields.io/github/last-commit/yezijinn/voice-player-zh-localized.svg)]()

一款面向中文听书场景的 Android 有声书播放器。基于开源项目 [Voice](https://github.com/PaulWoitaschek/Voice) 进行中文本地化和功能增强，保留了原始项目的稳定基础，同时补齐了更适合中文用户的界面与常用播放能力。

## 最新发布

- 当前最新版本：**Jinn有声书播放器 v20260703**
- 下载地址：<https://github.com/yezijinn/voice-player-zh-localized/releases>

## 主要能力

| 功能 | 说明 |
|------|------|
| 中文界面 | 全站中文本地化，仅保留简体中文，适合直接上手 |
| 定时关闭 | 支持按时间停止播放 |
| 定集关闭 | 播放到指定集数后自动停止，计算剩余时长时自动扣除跳过片头片尾的部分 |
| 跳过片头片尾 | 可自定义片头、片尾跳过时长，与定集关闭联动 |
| 检查更新 | 设置页二次开发信息中可直接跳转 Release 页面检查更新 |
| 书签 | 记录并快速回到播放位置 |
| 主题 | 支持多种主题配色 |
| 桌面小部件 | 快速控制播放状态 |

## 定集关闭与跳过片头片尾联动

定集关闭功能在计算剩余播放时长时，会自动考虑用户设置的跳过片头和跳过片尾时长，使倒计时更准确。

举例：每集5分钟，跳过片头30秒，跳过片尾10秒，当前在第1集2分钟处，定2集关闭：

- 第1集剩余有效时长：3:00 - 10秒(片尾) = 2分50秒
- 第2集有效全长：5:00 - 30秒(片头) - 10秒(片尾) = 4分20秒
- 总倒计时时长：7分10秒（而非原始的8分钟）

如果未开启跳过片头片尾，则保持原始计算方法不变。播放器界面的进度条和时长显示始终为原始音频时长，不受跳过设置影响。

## 截图展示

| 欢迎页面 | 应用主页 |
|:---:|:---:|
| ![Welcome](Voice-main/screenshots/welcome.jpg) | ![Home](Voice-main/screenshots/home.jpg) |

| 播放页面 | 设置页面 |
|:---:|:---:|
| ![Player](Voice-main/screenshots/player.jpg) | ![Settings](Voice-main/screenshots/settings.jpg) |

| 定时功能 | 关于 |
|:---:|:---:|
| ![Timer](Voice-main/screenshots/timer.jpg) | ![About](Voice-main/screenshots/about.jpg) |

## 安装与编译

### 下载安装包

直接到 Releases 页面下载最新安装包：<https://github.com/yezijinn/voice-player-zh-localized/releases>

### 本地编译

如果需要自行编译或二次修改，请把项目依赖放到 `tools/` 目录，然后运行：

```bash
python 一键打包.py
```

脚本会把完整编译日志写入根目录的 `build.log`，编译成功后自动生成可安装 APK。

### 自行编译详细步骤

#### 1. 下载编译环境

项目需要以下依赖，请按顺序下载并解压到项目根目录的 `tools/` 文件夹：

| 依赖 | 版本 | 下载链接 | 解压后目录 |
|------|------|----------|------------|
| JDK | 25.0.3 | [OpenJDK 25](https://github.com/adoptium/temurin25-binaries/releases/download/jdk-25.0.3%2B9/OpenJDK25U-jdk_x64_windows_hotspot_25.0.3_9.zip) | `tools/jdk/` |
| Gradle | 9.6.1 | [Gradle 9.6.1](https://services.gradle.org/distributions/gradle-9.6.1-all.zip) | `tools/gradle/` |
| Android SDK | API 37 | [Android SDK 命令行工具](https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip) + [platforms/android-37](https://dl.google.com/android/repository/platform-37_r02.zip) + [build-tools/37.0.0](https://dl.google.com/android/repository/build-tools_r37.zip) | `tools/android-sdk/` |

详细安装步骤：

```bash
# 1. 进入项目目录
cd voice-player-zh-localized

# 2. 创建 tools 目录
mkdir tools

# 3. 下载并解压 JDK 25，将文件夹重命名为 jdk，放入 tools 目录
#    最终路径: tools/jdk/bin/java.exe

# 4. 下载并解压 Gradle 9.6.1，将文件夹重命名为 gradle，放入 tools 目录
#    最终路径: tools/gradle/bin/gradle.bat

# 5. 下载并配置 Android SDK
#    5.1 下载命令行工具，解压到 tools/android-sdk/cmdline-tools/
#    5.2 重命名文件夹为 latest
#    5.3 使用 sdkmanager 安装平台:
#        tools\android-sdk\cmdline-tools\latest\bin\sdkmanager.bat "platforms;android-37" "build-tools;37.0.0"

# 6. 创建 local.properties
#    sdk.dir=D:\path\to\voice-player-zh-localized\tools\android-sdk
```

#### 2. 运行编译

```bash
python 一键打包.py
```

首次运行会下载必要的 Gradle 组件（大约 100-200MB），编译成功后 APK 文件会生成在项目根目录。

#### 3. 生成签名密钥

Android 应用安装到手机时需要数字签名。首次编译需要生成签名密钥：

```bash
keytool.exe -genkeypair -v -keystore signing.jks -alias 你的别名 -keyalg RSA -keysize 2048 -validity 10000 -storepass 你的密码 -keypass 你的密码 -dname "CN=你的名字, OU=Development, O=YourName, L=City, ST=State, C=CN"
```

然后在项目根目录创建 `signing.properties`：

```
STORE_PASSWORD=你的密码
KEY_ALIAS=你的别名
KEY_PASSWORD=你的密码
```

配置完成后重新运行 `python 一键打包.py` 即可生成带签名的 APK。

请务必备份签名密钥文件（`signing.jks`）和密码，丢失后将无法生成相同签名的更新包。

## 项目结构

```
Voice-main/
├── app/                    # 主应用模块
├── core/                   # 核心模块
│   ├── common/            # 通用工具
│   ├── data/              # 数据层 (Room数据库)
│   ├── playback/          # 播放引擎 (ExoPlayer)
│   ├── scanner/           # 媒体文件扫描
│   ├── search/            # 搜索功能
│   ├── sleeptimer/        # 定时功能
│   └── ui/                # UI组件
├── features/               # 功能模块
│   ├── bookOverview/      # 书籍列表
│   ├── bookmark/          # 书签
│   ├── playbackScreen/    # 播放界面
│   ├── settings/          # 设置
│   ├── sleepTimer/        # 定时设置
│   └── widget/            # 桌面小部件
└── signing/                # 签名配置
```

## 技术栈

- **语言**：Kotlin
- **UI框架**：Jetpack Compose
- **架构**：MVVM + Clean Architecture
- **依赖注入**：Metro (Dagger)
- **数据库**：Room
- **媒体播放**：ExoPlayer (Media3)
- **构建工具**：Gradle 9.6.1

## 更新日志

### v20260703

- 定集关闭功能：计算剩余播放时长时自动扣除跳过片头片尾的部分，倒计时更准确
- 设置页新增"检查更新"按钮，可直接跳转 GitHub Release 页面
- 全站仅保留简体中文，移除其他语言资源
- 播放器界面时长显示保持原始音频时长，不受跳过设置影响

### v20260702

- 初始中文本地化版本
- 定时关闭、定集停止、跳过片头片尾等基础功能
- 一键打包编译脚本

## 许可证

本项目基于 [GPL-3.0](LICENSE.md) 许可证开源。

## 致谢

- 原始项目：[Voice](https://github.com/PaulWoitaschek/Voice) by Paul Woitaschek
- 感谢所有开源贡献者
