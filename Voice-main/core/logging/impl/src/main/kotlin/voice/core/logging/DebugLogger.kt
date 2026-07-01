package voice.core.logging

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import voice.core.data.store.DebugLogStore
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * 调试日志工具类
 * 支持开关控制、Logcat输出、文件写入
 */
object DebugLogger {

  private const val TAG = "VoiceDebug"
  private const val MAX_LOG_FILE_SIZE = 10 * 1024 * 1024L // 10MB
  private const val MAX_LOG_FILES = 7 // 保留7天日志

  private var context: Context? = null
  private var dataStore: DataStore<Boolean>? = null
  private var isEnabled = false

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
  private val logQueue = ConcurrentLinkedQueue<LogEntry>()
  private var logFile: File? = null
  private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

  private val _enabledState = MutableStateFlow(false)
  val enabledState: StateFlow<Boolean> = _enabledState

  data class LogEntry(
    val level: String,
    val tag: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
  )

  /**
   * 初始化日志系统
   */
  fun init(ctx: Context, store: DataStore<Boolean>) {
    context = ctx.applicationContext
    dataStore = store

    // 读取保存的状态
    scope.launch {
      isEnabled = store.data.first()
      _enabledState.value = isEnabled
      if (isEnabled) {
        initLogFile()
      }
    }
  }

  /**
   * 设置日志开关状态
   */
  fun setEnabled(enabled: Boolean) {
    scope.launch {
      isEnabled = enabled
      _enabledState.value = enabled
      dataStore?.updateData { enabled }

      if (enabled) {
        initLogFile()
        i(TAG, "日志功能已开启")
      } else {
        i(TAG, "日志功能已关闭")
        flushLogs()
      }
    }
  }

  /**
   * 获取当前开关状态
   */
  fun isEnabled(): Boolean = isEnabled

  private fun initLogFile() {
    val ctx = context ?: return

    // 使用外部存储路径：/storage/emulated/0/Android/data/<包名>/files/logs/
    val externalFilesDir = ctx.getExternalFilesDir("logs")
    val logDir = if (externalFilesDir != null && externalFilesDir.canWrite()) {
      externalFilesDir
    } else {
      // 降级到内部存储
      File(ctx.filesDir, "logs")
    }

    if (!logDir.exists()) {
      logDir.mkdirs()
    }

    // 清理过期日志
    cleanOldLogs(logDir)

    // 创建新日志文件
    val dateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
    logFile = File(logDir, "voice_$dateStr.log")

    // 输出日志文件路径到 Logcat，方便调试
    Log.i(TAG, "日志文件路径: ${logFile.absolutePath}")
  }

  private fun cleanOldLogs(logDir: File) {
    val files = logDir.listFiles()?.sortedByDescending { it.lastModified() } ?: return
    if (files.size > MAX_LOG_FILES) {
      files.drop(MAX_LOG_FILES).forEach { it.delete() }
    }
  }

  /**
   * 记录 Info 级别日志
   */
  fun i(tag: String, message: String) {
    log("INFO", tag, message)
  }

  /**
   * 记录 Debug 级别日志
   */
  fun d(tag: String, message: String) {
    log("DEBUG", tag, message)
  }

  /**
   * 记录 Warning 级别日志
   */
  fun w(tag: String, message: String) {
    log("WARN", tag, message)
  }

  /**
   * 记录 Error 级别日志
   */
  fun e(tag: String, message: String, throwable: Throwable? = null) {
    val msg = if (throwable != null) "$message\n${Log.getStackTraceString(throwable)}" else message
    log("ERROR", tag, msg)
  }

  private fun log(level: String, tag: String, message: String) {
    if (!isEnabled) return

    val entry = LogEntry(level, tag, message)

    // 输出到 Logcat
    when (level) {
      "DEBUG" -> Log.d(tag, message)
      "INFO" -> Log.i(tag, message)
      "WARN" -> Log.w(tag, message)
      "ERROR" -> Log.e(tag, message)
    }

    // 加入队列等待写入文件
    logQueue.offer(entry)

    // 如果队列超过一定数量，刷新到文件
    if (logQueue.size >= 10) {
      flushLogs()
    }
  }

  /**
   * 刷新日志到文件
   */
  private fun flushLogs() {
    val file = logFile ?: return

    scope.launch(Dispatchers.IO) {
      val entries = mutableListOf<LogEntry>()
      while (logQueue.isNotEmpty()) {
        logQueue.poll()?.let { entries.add(it) }
      }

      if (entries.isEmpty()) return@launch

      // 检查文件大小
      if (file.exists() && file.length() > MAX_LOG_FILE_SIZE) {
        // 文件过大，创建一个新文件
        val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val newFile = File(file.parentFile, "voice_${dateStr}.log")
        logFile = newFile
      }

      // 写入日志
      val content = buildString {
        entries.forEach { entry ->
          appendLine("${dateFormat.format(Date(entry.timestamp))} ${entry.level}/${entry.tag}: ${entry.message}")
        }
      }

      try {
        file.appendText(content)
      } catch (e: Exception) {
        Log.e(TAG, "Failed to write log to file: ${e.message}")
      }
    }
  }

  /**
   * 获取日志文件路径
   */
  fun getLogFilePath(): String? {
    return logFile?.absolutePath
  }

  /**
   * 获取所有日志文件
   */
  fun getAllLogFiles(): List<File> {
    val ctx = context ?: return emptyList()
    val logDir = File(ctx.filesDir, "logs")
    return if (logDir.exists()) {
      logDir.listFiles()?.filter { it.extension == "log" }?.sortedByDescending { it.lastModified() } ?: emptyList()
    } else {
      emptyList()
    }
  }

  /**
   * 分享日志文件
   */
  fun shareLogs(): List<File> {
    return getAllLogFiles()
  }

  /**
   * 清理所有日志
   */
  fun clearLogs() {
    scope.launch(Dispatchers.IO) {
      getAllLogFiles().forEach { it.delete() }
      logQueue.clear()
    }
  }
}