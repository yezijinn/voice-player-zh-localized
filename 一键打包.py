#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
有声书播放器 - 一键打包脚本
=========================

功能说明:
    该脚本用于自动化编译有声书播放器Android APK应用。
    所有依赖项（JDK、Gradle、Android SDK）均安装在项目本地tools/目录，
    不依赖系统环境，不联网下载任何文件。
    
使用方法:
    python 一键打包.py
    
输出:
    - 编译日志输出到 build.log 文件
    - 最终APK文件保存在脚本同目录下，文件名格式: 有声书-YYYYMMDD_HHMMSS.apk
"""

import os
import shutil
import subprocess
import sys
import datetime
import glob
import logging
import threading
from typing import List


# ==================== 配置常量 ====================

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_DIR = os.path.join(SCRIPT_DIR, "Voice-main")
TOOLS_DIR = os.path.join(SCRIPT_DIR, "tools")

# 各工具目录路径
JDK_DIR = os.path.join(TOOLS_DIR, "jdk")
GRADLE_DIR = os.path.join(TOOLS_DIR, "gradle")
ANDROID_SDK_DIR = os.path.join(TOOLS_DIR, "android-sdk")

# 编译相关目录
BUILD_DIR = os.path.join(SCRIPT_DIR, "build-temp")
TEMP_DIR = os.path.join(SCRIPT_DIR, "kotlin-temp")
GRADLE_HOME = os.path.join(SCRIPT_DIR, ".gradle-home")

# 最终APK路径动态匹配
APK_PATTERN = os.path.join(
    BUILD_DIR, "app", "build", "outputs", "apk", "free", "release", "*.apk"
)

# 日志文件
LOG_FILE = os.path.join(SCRIPT_DIR, "build.log")


# ==================== 日志配置 ====================

def setup_logger() -> logging.Logger:
    """
    配置日志记录器
    
    Returns:
        logging.Logger: 配置好的日志记录器
    """
    logger = logging.getLogger("VoiceBuilder")
    logger.setLevel(logging.DEBUG)
    
    # 清除已有的处理器（防止重复添加）
    if logger.handlers:
        logger.handlers.clear()
    
    # 文件处理器 - 记录所有级别日志
    file_handler = logging.FileHandler(LOG_FILE, encoding="utf-8", mode="w")
    file_handler.setLevel(logging.DEBUG)
    file_formatter = logging.Formatter(
        "%(asctime)s | %(levelname)-8s | %(message)s",
        datefmt="%Y-%m-%d %H:%M:%S"
    )
    file_handler.setFormatter(file_formatter)
    
    # 控制台处理器 - 只记录INFO及以上级别
    console_handler = logging.StreamHandler()
    console_handler.setLevel(logging.INFO)
    console_formatter = logging.Formatter("%(message)s")
    console_handler.setFormatter(console_formatter)
    
    logger.addHandler(file_handler)
    logger.addHandler(console_handler)
    
    return logger


# 全局日志对象
logger = setup_logger()


# ==================== 工具函数 ====================

def decode_output(data: bytes, encoding: str = "utf-8") -> str:
    """
    解码subprocess输出的字节流
    处理 Windows 可能出现的编码问题（GBK/UTF-8）
    
    Args:
        data: 字节数据
        encoding: 编码格式，默认为utf-8
        
    Returns:
        str: 解码后的字符串
    """
    if not data:
        return ""
    
    # 尝试按指定编码解码
    try:
        return data.decode(encoding)
    except UnicodeDecodeError:
        # 如果失败，尝试其他编码
        for alt_encoding in ["gbk", "gb2312", "cp936", "utf-8", "latin1"]:
            try:
                return data.decode(alt_encoding, errors="replace")
            except:
                continue
        # 最终fallback
        return data.decode("utf-8", errors="replace")


def stream_reader(stream, logger_func, stream_writer):
    """
    异步流读取器（线程专用）
    实时读取子进程输出，解码后写入控制台和日志
    
    Args:
        stream: 子进程的 stdout 或 stderr 流
        logger_func: 日志记录函数 (logger.info 或 logger.error)
        stream_writer: 输出目标 (sys.stdout 或 sys.stderr)
    """
    # 使用 iter(readline, sentinel) 模式高效读取
    for raw_line in iter(stream.readline, b''):
        if not raw_line:
            break
        
        # 解码字节流
        try:
            decoded = decode_output(raw_line).rstrip('\r\n')
        except:
            decoded = str(raw_line)
        
        if decoded.strip():
            # 实时输出到控制台
            print(decoded, file=stream_writer)
            
            # 实时写入日志文件
            logger_func(decoded)


# ==================== 依赖检查函数 ====================

def check_dependency(name: str, path: str, required_files: List[str]) -> bool:
    """
    检查项目本地依赖是否存在
    
    Args:
        name: 依赖项名称（如JDK、Gradle、Android SDK）
        path: 依赖项安装路径
        required_files: 必须存在的文件列表
        
    Returns:
        bool: 依赖项是否完整存在
    """
    logger.info(f"正在检查 {name}...")
    logger.debug(f"检查路径: {path}")
    
    missing = []
    for f in required_files:
        full_path = os.path.join(path, f)
        if not os.path.exists(full_path):
            missing.append(f)
            logger.debug(f"  缺少: {full_path}")
    
    if missing:
        logger.error(f"✗ {name} 检查失败！缺少以下文件:")
        for f in missing:
            logger.error(f"    - {f}")
        logger.error(f"请将 {name} 安装到: {path}")
        return False
    
    logger.info(f"✓ {name} 检查通过")
    logger.debug(f"  路径: {path}")
    return True


def ensure_jdk() -> str:
    """
    确保项目本地 JDK 已安装
    
    Returns:
        str: Java可执行文件路径
    """
    if not check_dependency(
        "JDK", 
        JDK_DIR, 
        ["bin/java.exe", "bin/javac.exe"]
    ):
        logger.error("JDK 下载地址:")
        logger.error(
            "https://github.com/adoptium/temurin25-binaries/releases/"
            "download/jdk-25.0.3%2B9/OpenJDK25U-jdk_x64_windows_hotspot_25.0.3_9.zip"
        )
        sys.exit(1)
    
    return os.path.join(JDK_DIR, "bin", "java.exe")


def ensure_gradle() -> str:
    """
    确保项目本地 Gradle 已安装
    
    Returns:
        str: Gradle可执行文件路径
    """
    if not check_dependency(
        "Gradle", 
        GRADLE_DIR, 
        ["bin/gradle.bat"]
    ):
        logger.error("Gradle 下载地址:")
        logger.error("https://services.gradle.org/distributions/gradle-9.6.1-all.zip")
        sys.exit(1)
    
    return os.path.join(GRADLE_DIR, "bin", "gradle.bat")


def ensure_android_sdk() -> None:
    """
    确保项目本地 Android SDK 已安装
    """
    if not check_dependency(
        "Android SDK", 
        ANDROID_SDK_DIR, 
        ["platforms/android-37.0/android.jar", "build-tools/37.0.0/aapt.exe"]
    ):
        logger.error(f"请将 Android SDK 复制到: {ANDROID_SDK_DIR}")
        sys.exit(1)


# ==================== 编译辅助函数 ====================

def sync_source() -> None:
    """
    同步源代码到编译目录
    删除旧的构建产物，复制最新的源代码（包含签名目录）
    """
    logger.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    logger.info("[4/7] 同步源代码到编译目录")
    logger.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    logger.debug(f"源目录: {PROJECT_DIR}")
    logger.debug(f"目标目录: {BUILD_DIR}")
    
    # 保留的目录（不删除）
    preserve_dirs = {".gradle", ".git", "build", ".kotlin"}
    
    # 清理目标目录
    if os.path.isdir(BUILD_DIR):
        for item in os.listdir(BUILD_DIR):
            if item in preserve_dirs:
                continue
            item_path = os.path.join(BUILD_DIR, item)
            try:
                if os.path.isdir(item_path):
                    shutil.rmtree(item_path, ignore_errors=True)
                else:
                    os.remove(item_path)
                logger.debug(f"  已清理: {item}")
            except Exception as e:
                logger.warning(f"  清理 {item} 失败: {e}")
    else:
        os.makedirs(BUILD_DIR, exist_ok=True)
    
    # 同步源代码（包含 signing 目录用于签名）
    ignore_patterns = shutil.ignore_patterns(".gradle", ".git", "build", ".kotlin")
    shutil.copytree(
        PROJECT_DIR,
        BUILD_DIR,
        symlinks=False,
        ignore=ignore_patterns,
        dirs_exist_ok=True,
    )
    
    logger.info("✓ 源代码同步成功")


def setup_local_properties() -> None:
    """
    创建 local.properties 文件
    配置指向项目本地 Android SDK 路径
    """
    logger.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    logger.info("[5/7] 配置 Android SDK 路径")
    logger.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    
    local_props = os.path.join(BUILD_DIR, "local.properties")
    sdk_path = ANDROID_SDK_DIR.replace("\\", "/")
    
    try:
        with open(local_props, "w", encoding="utf-8") as f:
            f.write(f"sdk.dir={sdk_path}\n")
        logger.debug(f"  已创建文件: {local_props}")
        logger.debug(f"  sdk.dir={sdk_path}")
        logger.info("✓ local.properties 已创建")
    except Exception as e:
        logger.error(f"✗ 创建 local.properties 失败: {e}")
        sys.exit(1)


def setup_gradle_wrapper() -> None:
    """
    修改 gradle-wrapper.properties
    配置使用本地 Gradle zip 文件而非联网下载
    """
    logger.debug("配置 Gradle wrapper...")
    
    wrapper_props = os.path.join(BUILD_DIR, "gradle", "wrapper", "gradle-wrapper.properties")
    
    if not os.path.isfile(wrapper_props):
        logger.warning("  gradle-wrapper.properties 不存在，跳过配置")
        return
    
    try:
        with open(wrapper_props, "r", encoding="utf-8") as f:
            content = f.read()
        
        local_zip = os.path.join(TOOLS_DIR, "gradle-local.zip").replace(os.sep, "/")
        
        # 替换下载URL为本地路径
        original_url = "distributionUrl=https\\://services.gradle.org/distributions/gradle-9.6.1-all.zip"
        new_url = f"distributionUrl=file:///{local_zip}"
        
        if original_url in content:
            content = content.replace(original_url, new_url)
            
            with open(wrapper_props, "w", encoding="utf-8") as f:
                f.write(content)
            
            logger.debug(f"  已配置 Gradle wrapper 使用本地 zip: {local_zip}")
        else:
            logger.debug("  Gradle wrapper 已配置为本地，无需修改")
    except Exception as e:
        logger.warning(f"  配置 Gradle wrapper 失败: {e}")


def get_build_environment() -> dict:
    """
    构建编译所需的环境变量
    
    Returns:
        dict: 环境变量字典
    """
    env = os.environ.copy()
    
    # Java 相关环境变量
    env["JAVA_HOME"] = JDK_DIR
    env["PATH"] = os.path.join(JDK_DIR, "bin") + os.pathsep + env.get("PATH", "")
    env["JAVA_TOOL_OPTIONS"] = f"-Djava.io.tmpdir={TEMP_DIR}"
    
    # Gradle 相关环境变量
    env["GRADLE_HOME"] = GRADLE_DIR
    env["GRADLE_USER_HOME"] = GRADLE_HOME
    
    # Android SDK 相关环境变量
    env["ANDROID_HOME"] = ANDROID_SDK_DIR
    env["ANDROID_SDK_ROOT"] = ANDROID_SDK_DIR
    
    # Kotlin 临时目录
    env["KOTLIN_TEMP_DIR"] = TEMP_DIR
    
    # 确保临时目录存在
    os.makedirs(TEMP_DIR, exist_ok=True)
    os.makedirs(GRADLE_HOME, exist_ok=True)
    
    logger.debug("环境变量已配置:")
    logger.debug(f"  JAVA_HOME={env['JAVA_HOME']}")
    logger.debug(f"  GRADLE_HOME={env['GRADLE_HOME']}")
    logger.debug(f"  ANDROID_HOME={env['ANDROID_HOME']}")
    logger.debug(f"  GRADLE_USER_HOME={env['GRADLE_USER_HOME']}")
    logger.debug(f"  TEMP_DIR={TEMP_DIR}")
    
    return env


def build_apk(jdk_bin: str, gradle_bin: str) -> None:
    """
    编译 APK (多线程异步版本)
    使用本地 Gradle 直接编译，完全绕过 wrapper
    
    Args:
        jdk_bin: Java 可执行文件路径
        gradle_bin: Gradle 可执行文件路径
    """
    logger.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    logger.info("[6/7] 开始编译 APK")
    logger.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    logger.info(f"Java: {jdk_bin}")
    logger.info(f"Gradle: {gradle_bin}")
    
    # 配置 Gradle wrapper 使用本地文件
    setup_gradle_wrapper()
    
    # 设置环境变量
    env = get_build_environment()
    
    # 构建命令
    cmd = [
        gradle_bin,
        "-p", BUILD_DIR,
        "assembleFreeRelease",
        "--no-daemon",
        "--no-build-cache"
    ]
    
    logger.debug(f"执行命令: {' '.join(cmd)}")
    logger.info("")
    logger.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    logger.info("编译输出（实时流式显示）:")
    logger.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    
    # 启动子进程
    process = subprocess.Popen(
        cmd,
        cwd=BUILD_DIR,
        env=env,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        bufsize=0,
        text=False
    )
    
    # 创建并启动两个线程，分别读取 stdout 和 stderr
    t_stdout = threading.Thread(
        target=stream_reader, 
        args=(process.stdout, logger.info, sys.stdout)
    )
    t_stderr = threading.Thread(
        target=stream_reader, 
        args=(process.stderr, logger.error, sys.stderr)
    )
    
    # 设置为守护线程
    t_stdout.daemon = True
    t_stderr.daemon = True
    
    # 启动线程
    t_stdout.start()
    t_stderr.start()
    
    # 等待进程结束
    return_code = process.wait()
    
    # 等待线程完成（确保最后一部分输出也被写入日志）
    t_stdout.join(timeout=5.0)
    t_stderr.join(timeout=5.0)
    
    # 检查返回码
    if return_code != 0:
        logger.error("✗ 编译失败！")
        logger.error(f"Gradle 返回码: {return_code}")
        logger.error("请检查上方错误信息。")
        sys.exit(1)
    
    logger.info("")
    logger.info("✓ 编译成功")


def copy_apk() -> str:
    """
    查找编译生成的 APK 文件并复制到输出目录
    文件名包含时间戳
    
    Returns:
        str: 最终APK文件路径
    """
    logger.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    logger.info("[7/7] 复制 APK 到输出目录")
    logger.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    
    # 查找所有 APK 文件
    apk_files = glob.glob(APK_PATTERN)
    logger.debug(f"查找路径: {APK_PATTERN}")
    
    if not apk_files:
        logger.error("✗ 找不到编译生成的 APK 文件！")
        logger.error(f"查找路径: {APK_PATTERN}")
        logger.error("请检查编译是否成功，或确认 APK 的实际路径。")
        sys.exit(1)
    
    # 按修改时间排序，取最新的
    apk_src = max(apk_files, key=os.path.getmtime)
    apk_size = os.path.getsize(apk_src) / (1024 * 1024)
    logger.info(f"找到 APK: {os.path.basename(apk_src)}")
    logger.debug(f"完整路径: {apk_src}")
    logger.debug(f"原始大小: {apk_size:.2f} MB")
    
    # 生成时间戳
    timestamp = datetime.datetime.now().strftime("%Y%m%d_%H%M%S")
    new_filename = f"有声书-{timestamp}.apk"
    apk_dst = os.path.join(SCRIPT_DIR, new_filename)
    
    # 复制 APK
    shutil.copy2(apk_src, apk_dst)
    size_mb = os.path.getsize(apk_dst) / (1024 * 1024)
    
    logger.info("✓ APK 已保存到: " + apk_dst)
    logger.info(f"  文件大小: {size_mb:.2f} MB")
    
    return apk_dst


# ==================== 界面输出函数 ====================

def print_banner() -> None:
    """打印脚本横幅"""
    banner = """
╔══════════════════════════════════════════════════════════════════════════╗
║                                                                          ║
║              ╔══════════════════════════════════════════╗                ║
║              ║        有声书播放器   一键打包脚本            ║                ║
║              ╚══════════════════════════════════════════╝                ║
║                                                                          ║
║    ✓ 所有依赖安装在项目本地，不联网下载                                        ║
║    ✓ 编译日志将保存到 build.log 文件                                        ║
║                                                                          ║
╚══════════════════════════════════════════════════════════════════════════╝
"""
    logger.info(banner)


def print_summary(apk_path: str, elapsed_time: float) -> None:
    """
    打印构建总结
    
    Args:
        apk_path: 最终APK文件路径
        elapsed_time: 构建耗时（秒）
    """
    summary = f"""
╔══════════════════════════════════════════════════════════════════════════╗
║                              打包完成！                                    ║
╠══════════════════════════════════════════════════════════════════════════╣
║                                                                          ║
║  📦 APK 文件:                                                             ║
║      {apk_path:<64}                                                      ║
║                                                                          ║
║  📄 编译日志:                                                              ║
║      {LOG_FILE:<64}                                                      ║
║                                                                          ║
║  ⏱️  构建耗时: {elapsed_time:>6.2f} 秒                                     ║  
║                                                                          ║
╚══════════════════════════════════════════════════════════════════════════╝
"""
    logger.info(summary)


# ==================== 主函数 ====================

def main():
    """主函数：执行完整的打包流程"""
    print_banner()
    
    # 记录开始时间
    start_time = datetime.datetime.now()
    logger.info(f"构建开始时间: {start_time.strftime('%Y-%m-%d %H:%M:%S')}")
    logger.info("")
    
    try:
        # ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        # 步骤 1-3: 确保 JDK、Gradle、Android SDK 已安装到项目本地
        # ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        logger.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        logger.info("[步骤 1-3] 检查依赖项")
        logger.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        
        jdk_bin = ensure_jdk()
        gradle_bin = ensure_gradle()
        ensure_android_sdk()
        
        logger.info("✓ 所有依赖项检查完成")
        logger.info("")
        
        # ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        # 步骤 4: 同步源代码
        # ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        sync_source()
        logger.info("")
        
        # ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        # 步骤 5: 配置构建环境
        # ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        setup_local_properties()
        logger.info("")
        
        # ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        # 步骤 6: 编译 APK
        # ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        build_apk(jdk_bin, gradle_bin)
        logger.info("")
        
        # ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        # 步骤 7: 生成最终 APK
        # ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        final_apk = copy_apk()
        logger.info("")
        
        # 计算耗时
        end_time = datetime.datetime.now()
        elapsed = (end_time - start_time).total_seconds()
        
        logger.info(f"构建完成时间: {end_time.strftime('%Y-%m-%d %H:%M:%S')}")
        logger.info(f"总耗时: {elapsed:.2f} 秒")
        
        # 打印总结
        print_summary(final_apk, elapsed)
        
    except KeyboardInterrupt:
        logger.warning("")
        logger.warning("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        logger.warning("⚠ 用户中断构建")
        logger.warning("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        sys.exit(1)
    except Exception as e:
        logger.error("")
        logger.error("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        logger.error(f"✗ 构建过程中发生异常: {e}")
        logger.error("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        logger.exception("详细错误信息:")
        sys.exit(1)


if __name__ == "__main__":
    main()
