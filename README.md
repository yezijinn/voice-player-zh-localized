# Jinn有声书播放器

[![License](https://img.shields.io/badge/License-GPL--3.0-blue.svg)](LICENSE.md)
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)

> 基于 [Voice](https://github.com/PaulWoitaschek/Voice) 开源项目的二次开发版本，针对中文用户进行了全面本地化。

## 功能特性

- **中文界面**：100% 中文本地化
- **定时关闭**：支持按时间定时停止播放
- **定集关闭**：支持按集数自动停止播放（自动转为定时）
- **跳过片头片尾**：支持设置跳过时长，自动跳过片头片尾
- **应用版本**：自动取当前年月日作为版本号

## 下载安装

### 编译环境

| 依赖 | 版本 | 下载链接 |
|------|------|----------|
| JDK | 25 | [Temurin JDK 25](https://github.com/adoptium/temurin25-binaries/releases) |
| Gradle | 9.6.1 | [Gradle 9.6.1](https://services.gradle.org/distributions/gradle-9.6.1-all.zip) |
| Android SDK | API 37 | [Android Command Line Tools](https://developer.android.com/studio#command-tools) |

### 一键打包

```bash
python 一键打包.py
```

所有依赖（JDK、Gradle、Android SDK）需安装到项目本地 `tools/` 目录。

### 生成签名密钥（首次编译）

如果项目没有签名密钥，需要先生成：

```bash
# 进入项目目录
cd D:\TRAE_Project\Voice

# 生成签名密钥（替换为您自己的密码）
tools\jdk\bin\keytool.exe -genkeypair -v -keystore Voice-main\signing\my-release-key.jks -alias jinn -keyalg RSA -keysize 2048 -validity 9125 -storepass 您的密码 -keypass 您的密码 -dname "CN=您的名字, OU=Development, O=Jinn, L=Beijing, ST=Beijing, C=CN"

# 创建签名配置文件
echo STORE_PASSWORD=您的密码 > Voice-main\signing\signing.properties
echo KEY_ALIAS=jinn >> Voice-main\signing\signing.properties
echo KEY_PASSWORD=您的密码 >> Voice-main\signing\signing.properties
```

**重要**：
- 签名密钥文件（`.jks`）和 `signing.properties` 已配置在 `.gitignore` 中，不会推送到 GitHub
- 请妥善保管您的签名密钥和密码，丢失后将无法更新应用

## 项目结构

```
Voice-main/
├── app/                    # 主应用模块
├── core/                   # 核心模块
│   ├── common/            # 通用工具
│   ├── data/              # 数据层
│   ├── playback/          # 播放引擎
│   ├── scanner/           # 媒体扫描
│   ├── search/            # 搜索功能
│   ├── sleeptimer/        # 定时功能
│   └── ui/                # UI 组件
├── features/               # 功能模块
│   ├── bookOverview/      # 书籍列表界面
│   ├── bookmark/          # 书签功能
│   ├── cover/             # 封面处理
│   ├── folderPicker/      # 文件夹选择
│   ├── playbackScreen/    # 播放界面
│   ├── settings/          # 设置界面
│   ├── sleepTimer/        # 定时界面
│   └── widget/            # 桌面小部件
├── signing/                # 签名配置（不在GitHub中）
└── plugins/                # Gradle 插件
```

## 自定义修改

- [自定义修改UI指南](自定义修改UI指南.md)
- [开发文档](开发文档.md)

## 技术栈

- **语言**：Kotlin
- **UI 框架**：Jetpack Compose
- **依赖注入**：Metro
- **数据库**：Room
- **媒体播放**：ExoPlayer

## 许可证

本项目基于 [GPL-3.0](LICENSE.md) 许可证开源。

## 致谢

- 原始项目：[Voice](https://github.com/PaulWoitaschek/Voice) by Paul Woitaschek