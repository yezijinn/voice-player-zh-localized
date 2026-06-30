# 有声书播放器（中文本地化版）

基于 [Voice](https://github.com/PaulWoitaschek/Voice) 开源项目的二次开发版本，针对中文用户进行了全面本地化。

## 编译环境需求

| 依赖 | 版本 | 下载链接 |
|------|------|----------|
| JDK | 25 | https://github.com/adoptium/temurin25-binaries/releases |
| Gradle | 9.6.1 | https://services.gradle.org/distributions/gradle-9.6.1-all.zip |
| Android SDK | API 37 | https://developer.android.com/studio#command-tools |

## 一键打包

```bash
python 一键打包.py
```

所有依赖（JDK、Gradle、Android SDK）需安装到项目本地 `tools/` 目录。

## 主要特性

- 中文界面（100%）
- 定时关闭（按时间/按集数）
- 跳过片头片尾
- 定集关闭与跳过片尾联动
- 应用版本自动取当前年月日
