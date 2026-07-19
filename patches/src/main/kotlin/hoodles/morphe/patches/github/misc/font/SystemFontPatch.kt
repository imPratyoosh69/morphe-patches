package hoodles.morphe.patches.github.misc.font

import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.resourcePatch
import java.io.File

@Suppress("unused")
val systemFontPatch = resourcePatch(
    name = "Force system font",
    description = "Strips custom typography declarations to force the app to use the OS default system font.",
    default = true
) {
    compatibleWith(Compatibility(
        name = "GitHub",
        packageName = "com.github.android",
        appIconColor = 0x000000
    ))

    execute {
        // 'get' is provided by the Morphe DSL and returns a java.io.File wrapper.
        // Passing 'true' tells Apktool to decode the directory if it hasn't been yet.
        val resDir = get("res", true)

        // Recursively walk through all XML files in the resources directory
        resDir.walkTopDown().filter { it.isFile && it.extension == "xml" }.forEach { xmlFile ->
            val originalText = xmlFile.readText()

            // Only process files that actually mention a font family to save time
            if (originalText.contains("fontFamily")) {
                var patchedText = originalText

                // 1. Replace style/theme declarations in styles.xml
                // Changes <item name="fontFamily">@font/mona_sans</item> to sans-serif
                patchedText = patchedText.replace(
                    Regex("""<item name="android:fontFamily">[^<]+</item>"""),
                    """<item name="android:fontFamily">sans-serif</item>"""
                )
                patchedText = patchedText.replace(
                    Regex("""<item name="fontFamily">[^<]+</item>"""),
                    """<item name="fontFamily">sans-serif</item>"""
                )

                // 2. Replace hardcoded layout attributes in layout/*.xml
                // Changes app:fontFamily="@font/inter" to sans-serif
                patchedText = patchedText.replace(
                    Regex("""android:fontFamily="[^"]+""""),
                    """android:fontFamily="sans-serif""""
                )
                patchedText = patchedText.replace(
                    Regex("""app:fontFamily="[^"]+""""),
                    """app:fontFamily="sans-serif""""
                )

                // Save the file if modifications were made
                if (originalText != patchedText) {
                    xmlFile.writeText(patchedText)
                }
            }
        }
    }
}
