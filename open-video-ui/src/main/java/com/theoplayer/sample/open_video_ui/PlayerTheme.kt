package com.theoplayer.sample.open_video_ui

enum class PlayerTheme(val title: String, val description: String) {
    DEFAULT("Default", "Stock player theme with the `DefaultUI` controls"),
    CUSTOM_COLORS("Custom Colors", "`DefaultUI` with an orange/warm accent color scheme"),
    NITFLEX("Nitflex", "Full custom Netflix-style skin with red accents"),
    MINIMAL("Minimal", "Barebones player with only play/pause and a seek bar"),
    PORTRAIT("Portrait", "Fullscreen vertical player with side action buttons"),
    FESTIVE("Festive", "Holiday theme with red/green Christmas colors"),
    MODERN("Modern", "Modern-style player with pill-shaped controls")
}
