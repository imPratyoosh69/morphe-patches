package hoodles.morphe.patches.github.misc.font

import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.resourcePatch

@Suppress("unused")
val systemFontPatch = resourcePatch(
    name = "Force system font",
    description = "Strips custom typography declarations to force the app to use the true OS default system font.",
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

            // Added fontPath just in case they use third-party typography libraries
            if (originalText.contains("fontFamily") || originalText.contains("fontPath")) {
                var patchedText = originalText

                // 1. ERADICATE STYLES & THEMES
                // Removes the line entirely instead of hardcoding "sans-serif"
                patchedText = patchedText.replace(
                    Regex("""\s*<item name="android:fontFamily">[^<]+</item>"""),
                    ""
                )
                patchedText = patchedText.replace(
                    Regex("""\s*<item name="fontFamily">[^<]+</item>"""),
                    ""
                )

                // 2. ERADICATE HARDCODED LAYOUT ATTRIBUTES
                patchedText = patchedText.replace(
                    Regex("""\s*android:fontFamily="[^"]+""""),
                    ""
                )
                patchedText = patchedText.replace(
                    Regex("""\s*app:fontFamily="[^"]+""""),
                    ""
                )
                
                // 3. ERADICATE CUSTOM VIEW FONT PATHS
                patchedText = patchedText.replace(
                    Regex("""\s*app:fontPath="[^"]+""""),
                    ""
                )

                // Save the file if modifications were made
                if (originalText != patchedText) {
                    xmlFile.writeText(patchedText)
                }
            }
        }
    }
                    }
                    
