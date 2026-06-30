# Jinn有声书播放器 🎧

[![License](https://img.shields.io/badge/License-GPL--3.0-blue.svg)](LICENSE.md)
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Version](https://img.shields.io/badge/Version-2026.06.30-blue.svg)]()
[![Language](https://img.shields.io/badge/Language-Kotlin-purple.svg)]()
[![Stars](https://img.shields.io/github/stars/yezijinn/voice-player-zh-localized.svg)]()
[![Last Commit](https://img.shields.io/github/last-commit/yezijinn/voice-player-zh-localized.svg)]()

> 一款专为中文用户打造的开源有声书播放器，支持定时停止、定集停止、跳过片头片尾等实用功能。

---

## 📱 关于

**Jinn有声书播放器** 是一款功能强大的 Android 有声书应用，基于 [Voice](https://github.com/PaulWoitaschek/Voice) 开源项目二次开发，专为中文用户优化。

你是否遇到过这些问题？
- 📚 有声书太长，想定时停止却找不到合适的功能？
- 😴 睡前听书，想设置播放N集后自动停止？
- ⏭️ 每次都要手动跳过冗长的片头片尾？
- 🌐 想用中文界面，却找不到好用的播放器？

**Jinn有声书播放器** 就是为你而设计的！

---

## ✨ 核心功能

| 功能 | 说明 |
|------|------|
| 🇨🇳 **中文界面** | 100% 中文本地化，简洁易用 |
| ⏰ **定时关闭** | 按时间定时停止播放（分钟/章节） |
| 📚 **定集停止** | 设置播放N集后自动停止，听书更省心 |
| ⏭️ **跳过片头片尾** | 自定义跳过时长，自动跳过冗余内容 |
| 📖 **书签功能** | 随时保存播放位置，不错过任何精彩 |
| 🎨 **主题定制** | 多种主题色可选，个性化你的播放器 |
| 📱 **桌面小部件** | 支持桌面小组件，快速控制播放 |

---

## 📸 截图展示

| 欢迎页面 | 应用主页 |
|:---:|:---:|
| ![Welcome](screenshots/welcome.jpg) | ![Home](screenshots/home.jpg) |

| 播放页面 | 设置页面 |
|:---:|:---:|
| ![Player](screenshots/player.jpg) | ![Settings](screenshots/settings.jpg) |

| 定时功能 | 关于 |
|:---:|:---:|
| ![Timer](screenshots/timer.jpg) | ![About](screenshots/about.jpg) |

---

## 📥 下载安装

### 方式一：下载预编译 APK

在 [Releases](https://github.com/yezijinn/voice-player-zh-localized/releases) 页面下载最新 APK 文件，直接安装到手机即可使用。

### 方式二：自行编译

```bash
# 克隆项目
git clone https://github.com/yezijinn/voice-player-zh-localized.git
cd voice-player-zh-localized

# 运行一键打包脚本
python 一键打包.py
```

编译完成后，APK 文件将生成在项目根目录。

### 编译环境要求

| 依赖 | 版本 | 说明 |
|------|------|------|
| JDK | 25 | Android 开发必需 |
| Gradle | 9.6.1 | 构建工具 |
| Android SDK | API 37 | 编译 Android 37 |

> 💡 **提示**：所有依赖已配置为本地化，无需联网下载，一键打包脚本可自动处理。

---

## 🔧 首次编译 - 生成签名密钥

首次编译需要生成签名密钥（用于给 APK 签名）：

```bash
# 进入项目目录
cd D:\TRAE_Project\Voice

# 生成签名密钥（将密码替换为您自己的密码）
tools\jdk\bin\keytool.exe -genkeypair -v -keystore Voice-main\signing\my-release-key.jks -alias jinn -keyalg RSA -keysize 2048 -validity 9125 -storepass 您的密码 -keypass 您的密码 -dname "CN=您的名字, OU=Development, O=Jinn, L=Beijing, ST=Beijing, C=CN"

# 创建签名配置文件
echo STORE_PASSWORD=您的密码 > Voice-main\signing\signing.properties
echo KEY_ALIAS=jinn >> Voice-main\signing\signing.properties
echo KEY_PASSWORD=您的密码 >> Voice-main\signing\signing.properties
```

⚠️ **重要提示**：
- 签名密钥文件已添加到 `.gitignore`，不会推送到 GitHub
- 请务必备份您的签名密钥和密码，丢失后将无法更新应用

---

## 🏗️ 项目结构

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

---

## 🛠️ 技术栈

- **语言**：Kotlin
- **UI框架**：Jetpack Compose
- **架构**：MVVM + Clean Architecture
- **依赖注入**：Metro (Dagger)
- **数据库**：Room
- **媒体播放**：ExoPlayer (Media3)
- **构建工具**：Gradle 9.6.1

---

## 📚 文档

- [自定义修改UI指南](自定义修改UI指南.md) - 如何自定义界面
- [开发文档](开发文档.md) - 开发环境搭建和核心功能实现

---

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

---

## 📄 许可证

本项目基于 [GPL-3.0](LICENSE.md) 许可证开源。

---

## 💖 致谢

- 原始项目：[Voice](https://github.com/PaulWoitaschek/Voice) by Paul Woitaschek
- 感谢所有开源贡献者

---

**如果你喜欢这个项目，请点个 ⭐ Star 支持一下！**