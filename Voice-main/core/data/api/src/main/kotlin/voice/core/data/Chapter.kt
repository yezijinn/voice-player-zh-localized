package voice.core.data

import android.net.Uri
import androidx.core.net.toUri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant

@Entity(tableName = "chapters2")
public data class Chapter(
  @PrimaryKey
  val id: ChapterId,
  val name: String?,
  val duration: Long,
  val fileLastModified: Instant,
  @ColumnInfo(defaultValue = "0")
  val fileSize: Long,
  val markData: List<MarkData>,
) : Comparable<Chapter> {
  @Ignore
  val chapterMarks: List<ChapterMark> = parseMarkData()

  override fun compareTo(other: Chapter): Int {
    return id.compareTo(other.id)
  }
}

public object ChapterDurationHelper {
  public fun effectiveDuration(
    chapter: Chapter,
    skipStartSeconds: Int,
    skipEndSeconds: Int,
  ): Long {
    val startSkipMs = skipStartSeconds.coerceAtLeast(0) * 1000L
    val endSkipMs = skipEndSeconds.coerceAtLeast(0) * 1000L
    return (chapter.duration - startSkipMs - endSkipMs).coerceAtLeast(0L)
  }

  public fun remainingEffectiveDuration(
    chapter: Chapter,
    positionInChapterMs: Long,
    skipStartSeconds: Int,
    skipEndSeconds: Int,
  ): Long {
    val startSkipMs = skipStartSeconds.coerceAtLeast(0) * 1000L
    val endSkipMs = skipEndSeconds.coerceAtLeast(0) * 1000L
    val effectiveStartMs = startSkipMs.coerceAtMost(chapter.duration)
    val effectiveEndMs = (chapter.duration - endSkipMs).coerceAtLeast(effectiveStartMs)
    val currentPosition = positionInChapterMs.coerceAtLeast(effectiveStartMs)
    return (effectiveEndMs - currentPosition).coerceAtLeast(0L)
  }
}

private fun Chapter.parseMarkData(): List<ChapterMark> {
  return try {
    val positions = markData.map { it.startMs }.toSet()
    val sorted = markData.filterNot { it.startMs - 1 in positions || it.startMs < 0 }
      .distinctBy { it.startMs }
      .sortedBy { it.startMs }
    val maxEndMs = (duration - 1).coerceAtLeast(1)
    if (sorted.isEmpty()) {
      return listOf(ChapterMark(name, 0L, maxEndMs))
    }

    val result = mutableListOf<ChapterMark>()
    for ((index, mark) in sorted.withIndex()) {
      val name = mark.name
      val previous = result.lastOrNull()
      val next = sorted.getOrNull(index + 1)

      val endMs = if (next != null && next.startMs <= duration - 2) {
        next.startMs - 1
      } else {
        maxEndMs
      }

      if (previous == null) {
        result += ChapterMark(
          name = name,
          startMs = 0L,
          endMs = endMs,
        )
      } else if (previous.endMs + 1 < duration && previous.endMs + 1 < endMs) {
        result += ChapterMark(
          name = name,
          startMs = previous.endMs + 1,
          endMs = endMs,
        )
      }
    }
    result
  } catch (e: Exception) {
    throw IllegalStateException("Could not parse marks from $this", e)
  }
}

internal object ChapterIdSerializer : KSerializer<ChapterId> {

  override val descriptor: SerialDescriptor
    get() = PrimitiveSerialDescriptor("chapterId", PrimitiveKind.STRING)

  override fun deserialize(decoder: Decoder): ChapterId = ChapterId(decoder.decodeString())

  override fun serialize(
    encoder: Encoder,
    value: ChapterId,
  ) {
    encoder.encodeString(value.value)
  }
}

public fun ChapterId.toUri(): Uri = value.toUri()
