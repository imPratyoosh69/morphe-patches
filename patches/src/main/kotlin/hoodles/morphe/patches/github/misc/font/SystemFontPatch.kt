package hoodles.morphe.patches.github.misc.font

import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.resourcePatch

@Suppress("unused")
val forceSystemFontPatch = resourcePatch(
    name = "Force system font",
    description = "Forces the app to use applied system font.",
    default = true
) {
    // Target any version of GitHub
    compatibleWith(Compatibility(
        name = "GitHub",
        packageName = "com.github.android",
        appIconColor = 0x000000
    ))

    execute {
        // 1. NEUTRALIZE STYLES & THEMES
        // This modifies resources.arsc. By renaming "fontFamily" to a dummy string,
        // Android ignores the custom font requests and falls back to the system default.
        resourceTable.stringPool.strings.forEachIndexed { index, string ->
            if (string == "fontFamily" || string == "android:fontFamily") {
                resourceTable.stringPool.setString(index, "ignoredFont")
            }
        }

        // 2. NEUTRALIZE HARDCODED LAYOUTS
        // GitHub sometimes hardcodes fonts directly onto TextViews in layout files.
        // We iterate through every compiled XML file and neutralize them there too.
        xmlFiles.forEach { xmlFile ->
            // Skip non-layout XMLs to speed up patching
            if (xmlFile.name.startsWith("res/layout")) {
                var modified = false
                xmlFile.stringPool.strings.forEachIndexed { index, string ->
                    if (string == "fontFamily" || string == "android:fontFamily") {
                        xmlFile.stringPool.setString(index, "ignoredFont")
                        modified = true
                    }
                }
                // Save the file only if we actually changed something
                if (modified) {
                    xmlFile.save()
                }
            }
        }
    }
}
