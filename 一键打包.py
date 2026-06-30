#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
有声书播放器 - 一键打包脚本
用法: python 一键打包.py

所有依赖（JDK、Gradle、Android SDK）都安装在项目本地 tools/ 目录，不依赖系统环境。
不联网下载任何文件。
"""

import os
import shutil
import subprocess
import sys
import datetime
import glob  # 新增用于查找 APK

# ========== 项目本地路径配置 ==========
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_DIR = os.path.join(SCRIPT_DIR, "Voice-main")
TOOLS_DIR = os.path.join(SCRIPT_DIR, "tools")
JDK_DIR = os.path.join(TOOLS_DIR, "jdk")
GRADLE_DIR = os.path.join(TOOLS_DIR, "gradle")
ANDROID_SDK_DIR = os.path.join(TOOLS_DIR, "android-sdk")
BUILD_DIR = os.path.join(SCRIPT_DIR, "build-temp")
TEMP_DIR = os.path.join(SCRIPT_DIR, "kotlin-temp")
# 改为动态查找，不再写死
APK_PATTERN = os.path.join(BUILD_DIR, "app", "build", "outputs", "apk", "free", "release", "*.apk")


def check_dependency(name, path, required_files):
    """检查项目本地依赖是否存在"""
    missing = []
    for f in required_files:
        full_path = os.path.join(path, f)
        if not os.path.exists(full_path):
            missing.append(f)
    if missing:
        print(f"[错误] 项目本地未找到 {name}！")
        print(f"       缺少文件: {', '.join(missing)}")
        print(f"       请将 {name} 安装到: {path}")
        return False
    print(f"[信息] {name} 已存在于项目本地: {path}")
    return True


def ensure_jdk():
    """确保项目本地 JDK 已安装"""
    if not check_dependency("JDK", JDK_DIR, ["bin/java.exe", "bin/javac.exe"]):
        print("       JDK 下载地址: https://github.com/adoptium/temurin25-binaries/releases/download/jdk-25.0.3%2B9/OpenJDK25U-jdk_x64_windows_hotspot_25.0.3_9.zip")
        sys.exit(1)
    return os.path.join(JDK_DIR, "bin", "java.exe")


def ensure_gradle():
    """确保项目本地 Gradle 已安装"""
    if not check_dependency("Gradle", GRADLE_DIR, ["bin/gradle.bat"]):
        print("       Gradle 下载地址: https://services.gradle.org/distributions/gradle-9.6.1-all.zip")
        sys.exit(1)
    return os.path.join(GRADLE_DIR, "bin", "gradle.bat")


def ensure_android_sdk():
    """确保项目本地 Android SDK 已安装"""
    if not check_dependency("Android SDK", ANDROID_SDK_DIR, ["platforms/android-37.0/android.jar", "build-tools/37.0.0/aapt.exe"]):
        print("       请将 Android SDK 复制到: " + ANDROID_SDK_DIR)
        sys.exit(1)


def sync_source():
    """同步源代码到编译目录"""
    print("[4/6] 同步源代码到编译目录...")
    if os.path.isdir(BUILD_DIR):
        for item in os.listdir(BUILD_DIR):
            if item in (".gradle", ".git", "build", ".kotlin"):
                continue
            item_path = os.path.join(BUILD_DIR, item)
            if os.path.isdir(item_path):
                shutil.rmtree(item_path, ignore_errors=True)
            else:
                os.remove(item_path)
    shutil.copytree(
        PROJECT_DIR,
        BUILD_DIR,
        symlinks=False,
        ignore=shutil.ignore_patterns(".gradle", ".git", "build", ".kotlin"),
        dirs_exist_ok=True,
    )
    print("[完成] 源代码同步成功")


def setup_local_properties():
    """创建 local.properties 指向项目本地 Android SDK"""
    print("[5/6] 配置 Android SDK 路径...")
    local_props = os.path.join(BUILD_DIR, "local.properties")
    sdk_path = ANDROID_SDK_DIR.replace("\\", "/")
    with open(local_props, "w", encoding="utf-8") as f:
        f.write(f"sdk.dir={sdk_path}\n")
    print(f"[完成] local.properties 已创建: sdk.dir={sdk_path}")


def build_apk(jdk_bin, gradle_bin):
    """编译 APK - 直接使用本地 Gradle，完全绕过 wrapper"""
    print("[6/6] 开始编译 APK...")

    # 设置环境变量 - 全部指向项目本地
    env = os.environ.copy()
    env["JAVA_HOME"] = JDK_DIR
    env["PATH"] = os.path.join(JDK_DIR, "bin") + os.pathsep + env.get("PATH", "")
    env["GRADLE_HOME"] = GRADLE_DIR
    env["GRADLE_USER_HOME"] = os.path.join(SCRIPT_DIR, ".gradle-home")
    env["ANDROID_HOME"] = ANDROID_SDK_DIR
    env["ANDROID_SDK_ROOT"] = ANDROID_SDK_DIR
    env["JAVA_TOOL_OPTIONS"] = f"-Djava.io.tmpdir={TEMP_DIR}"
    env["KOTLIN_TEMP_DIR"] = TEMP_DIR

    os.makedirs(TEMP_DIR, exist_ok=True)
    os.makedirs(env["GRADLE_USER_HOME"], exist_ok=True)

    # 修改 gradle-wrapper.properties，指向本地 Gradle zip
    wrapper_props = os.path.join(BUILD_DIR, "gradle", "wrapper", "gradle-wrapper.properties")
    if os.path.isfile(wrapper_props):
        with open(wrapper_props, "r", encoding="utf-8") as f:
            content = f.read()
        local_zip = os.path.join(TOOLS_DIR, "gradle-local.zip").replace(os.sep, "/")
        content = content.replace(
            "distributionUrl=https\\://services.gradle.org/distributions/gradle-9.6.1-all.zip",
            f"distributionUrl=file:///{local_zip}",
        )
        with open(wrapper_props, "w", encoding="utf-8") as f:
            f.write(content)
        print(f"[信息] 已配置 Gradle wrapper 使用本地 zip")

    # 使用本地 Gradle 直接编译 - 改为 Release 版本
    cmd = [gradle_bin, "-p", BUILD_DIR, "assembleFreeRelease", "--no-daemon", "--no-build-cache"]

    result = subprocess.run(
        cmd,
        cwd=BUILD_DIR,
        env=env,
        capture_output=False,
    )

    if result.returncode != 0:
        print()
        print("[错误] 编译失败！请检查上方错误信息。")
        sys.exit(1)

    print("[完成] 编译成功")


def copy_apk():
    """动态查找 APK 并复制到输出目录，文件名包含时间戳"""
    print()
    print("[7/6] 复制 APK 到输出目录...")

    # 查找所有 APK 文件
    apk_files = glob.glob(APK_PATTERN)
    
    if not apk_files:
        print(f"[错误] 找不到编译生成的 APK 文件！")
        print(f"       查找路径: {APK_PATTERN}")
        print("       请检查编译是否成功，或确认 APK 的实际路径。")
        sys.exit(1)

    # 按修改时间排序，取最新的
    apk_src = max(apk_files, key=os.path.getmtime)
    print(f"[信息] 找到 APK: {os.path.basename(apk_src)}")

    # 生成时间戳（格式：年月日_时分秒）
    timestamp = datetime.datetime.now().strftime("%Y%m%d_%H%M%S")
    new_filename = f"有声书-{timestamp}.apk"
    apk_dst = os.path.join(SCRIPT_DIR, new_filename)

    # 复制 APK
    shutil.copy2(apk_src, apk_dst)
    size_mb = os.path.getsize(apk_dst) / (1024 * 1024)

    print(f"[完成] APK 已保存到: {apk_dst}")
    print(f"[信息] APK 大小: {size_mb:.2f} MB")
    return apk_dst


def main():
    print("=" * 50)
    print("  有声书播放器 - 一键打包脚本")
    print("  所有依赖安装在项目本地，不联网下载")
    print("=" * 50)
    print()

    # 步骤 1-3: 确保 JDK、Gradle、Android SDK 已安装到项目本地
    jdk_bin = ensure_jdk()
    gradle_bin = ensure_gradle()
    ensure_android_sdk()

    # 步骤 4: 同步源代码
    sync_source()

    # 步骤 5: 配置 Android SDK
    setup_local_properties()

    # 步骤 6: 编译
    build_apk(jdk_bin, gradle_bin)

    # 步骤 7: 复制 APK（带时间戳）
    final_apk = copy_apk()

    print()
    print("=" * 50)
    print(f"  打包完成！APK 文件：")
    print(f"  {final_apk}")
    print("=" * 50)


if __name__ == "__main__":
    main()