package hoodles.morphe.patches.github.misc.font

import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.resourcePatch

@Suppress("unused")
val systemFontPatch = resourcePatch(
    name = "Force system font",
    description = "Strips all typography and weight declarations to force 100% coverage of the OS default system font.",
    default = true
) {
    compatibleWith(Compatibility(
        name = "GitHub",
        packageName = "com.github.android",
        appIconColor = 0x000000
    ))

    execute {
        val resDir = get("res", true)

        resDir.walkTopDown().filter { it.isFile && it.extension == "xml" }.forEach { xmlFile ->
            val originalText = xmlFile.readText()

            // If it contains ANY typography-related keywords, process it
            if (originalText.contains("font") || originalText.contains("textStyle") || originalText.contains("typeface")) {
                var patchedText = originalText

                // 1. ERADICATE STYLES & THEMES
                val styleRegexes = listOf(
                    """\s*<item name="android:fontFamily">[^<]+</item>""",
                    """\s*<item name="fontFamily">[^<]+</item>""",
                    """\s*<item name="android:font">[^<]+</item>""",
                    """\s*<item name="font">[^<]+</item>""",
                    """\s*<item name="android:typeface">[^<]+</item>""",
                    """\s*<item name="typeface">[^<]+</item>""",
                    """\s*<item name="android:textStyle">[^<]+</item>""",
                    """\s*<item name="textStyle">[^<]+</item>"""
                )
                styleRegexes.forEach { regex ->
                    patchedText = patchedText.replace(Regex(regex), "")
                }

                // 2. ERADICATE HARDCODED LAYOUT ATTRIBUTES
                val attrRegexes = listOf(
                    """\s*android:fontFamily="[^"]+"""",
                    """\s*app:fontFamily="[^"]+"""",
                    """\s*android:font="[^"]+"""",
                    """\s*app:font="[^"]+"""",
                    """\s*app:fontPath="[^"]+"""",
                    """\s*android:typeface="[^"]+"""",
                    """\s*app:typeface="[^"]+"""",
                    """\s*android:textStyle="[^"]+"""",
                    """\s*app:textStyle="[^"]+""""
                )
                attrRegexes.forEach { regex ->
                    patchedText = patchedText.replace(Regex(regex), "")
                }

                // Save the file if modifications were made
                if (originalText != patchedText) {
                    xmlFile.writeText(patchedText)
                }
            }
        }
    }
}
