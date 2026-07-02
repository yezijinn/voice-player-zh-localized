#!/usr/bin/env kotlin

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

val repoRoot = File(".").canonicalFile
val stringsDir = File(repoRoot, "core/strings/src/main/res")
val minCoverage = 100
val baseLocales = setOf("zh-CN")
val documentBuilderFactory = DocumentBuilderFactory.newInstance().apply {
  isNamespaceAware = true
}

fun resourceNames(file: File): Set<String> {
  val document = documentBuilderFactory.newDocumentBuilder().parse(file)
  val resources = document.documentElement.childNodes
  val names = mutableSetOf<String>()
  for (index in 0 until resources.length) {
    val node = resources.item(index)
    val name = node.attributes?.getNamedItem("name")?.nodeValue
    if (name != null) {
      names += name
    }
  }
  return names
}

fun resourceLocaleToBcp47(resourceLocale: String): String {
  if (resourceLocale.startsWith("b+")) {
    return resourceLocale.removePrefix("b+").split("+").joinToString("-")
  }

  val parts = resourceLocale.split("-r", limit = 2)
  val language = when (parts[0]) {
    "in" -> "id"
    "iw" -> "he"
    "ji" -> "yi"
    else -> parts[0]
  }
  return if (parts.size == 1) language else "$language-${parts[1]}"
}

fun configuredLocales(): Set<String> = baseLocales
data class LocaleCoverage(
  val locale: String,
  val present: Int,
  val total: Int,
) {
  val percentage: Double = present.toDouble() / total * 100
}

val baseResources = resourceNames(File(stringsDir, "values/strings.xml"))
val localizedCoverages = stringsDir.listFiles()
  .orEmpty()
  .filter { it.isDirectory && it.name.startsWith("values-") }
  .map { directory ->
    val resources = resourceNames(File(directory, "strings.xml"))
    LocaleCoverage(
      locale = resourceLocaleToBcp47(directory.name.removePrefix("values-")),
      present = (resources intersect baseResources).size,
      total = baseResources.size,
    )
  }
  .sortedBy { it.locale }

val expectedLocales = baseLocales + localizedCoverages
  .filter { it.percentage >= minCoverage }
  .map { it.locale }

val actualLocales = configuredLocales()
val missing = expectedLocales - actualLocales
val unexpected = actualLocales - expectedLocales

if (missing.isNotEmpty() || unexpected.isNotEmpty()) {
  println("Locale resources do not match expected coverage.")
  println("Expected locales (>= ${minCoverage}% coverage plus ${baseLocales.sorted().joinToString()}):")
  println(expectedLocales.sorted().joinToString(", "))
  println("Actual locales:")
  println(actualLocales.sorted().joinToString(", "))

  if (missing.isNotEmpty()) {
    println("Missing from expected locales:")
    missing.sorted().forEach { println("- $it") }
  }

  if (unexpected.isNotEmpty()) {
    println("Locales below coverage threshold:")
    unexpected.sorted().forEach { println("- $it") }
  }

  println("Coverage:")
  localizedCoverages.sortedByDescending { it.percentage }.forEach {
    println("- ${it.locale}: ${it.present}/${it.total} (${String.format("%.1f", it.percentage)}%)")
  }

  throw IllegalStateException("Update localization resources to match coverage expectations.")
}

println("Locale config matches localization coverage.")
localizedCoverages.sortedByDescending { it.percentage }.forEach {
  val status = if (it.percentage >= minCoverage) "included" else "excluded"
  println("- ${it.locale}: ${it.present}/${it.total} (${String.format("%.1f", it.percentage)}%) $status")
}
