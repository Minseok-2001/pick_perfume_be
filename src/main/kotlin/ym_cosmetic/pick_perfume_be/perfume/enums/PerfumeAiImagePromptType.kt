package ym_cosmetic.pick_perfume_be.perfume.enums

enum class PerfumeAiImagePromptType(
    val themeLabel: String,
    val visualFocus: String,
    val atmosphere: String
) {
    STORYBOARD_IMPRESSION(
        themeLabel = "narrative storyboard",
        visualFocus = "evocative vignettes with poetic details and sense of motion",
        atmosphere = "cinematic, softly diffused lighting with rich tonal depth"
    ),
    EDITORIAL_STILL_LIFE(
        themeLabel = "editorial still life",
        visualFocus = "art-directed composition with tactile props and dramatic shadows",
        atmosphere = "high-contrast studio lighting, crisp focus, luxury styling"
    ),
    CHROMATIC_AURA(
        themeLabel = "chromatic aura study",
        visualFocus = "abstract gradients, light trails, and color fields that convey scent",
        atmosphere = "immersive, luminous glow with subtle bokeh and layered translucency"
    );
}
