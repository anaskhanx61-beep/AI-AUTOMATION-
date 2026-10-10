package com.example.model

enum class ScriptTone(val displayName: String, val description: String, val icon: String) {
    VIRAL_PUNCHY("Viral & Punchy", "High retention, quick sentences, strong dopamine hooks", "🔥"),
    DRAMATIC_MYSTERY("Dramatic & Mystery", "Curiosity gaps, suspenseful pacing, cliffhanger vibe", "🕵️"),
    HUSTLE_MOTIVATION("Hustle & Mindset", "Energetic, commanding, stoic and direct advice", "⚡"),
    EDUCATIONAL_DEEP("Mind-Blowing Facts", "Intellectual curiosity, unexpected twists, rich science", "🧠"),
    COMEDY_WITTY("Funny & Sarcastic", "Relatable irony, humorous hyperbole, punchy punchlines", "😂"),
    SCIFI_FUTURE("Sci-Fi & Future", "Technological wonder, AI evolution, cosmic exploration", "🚀")
}

enum class VisualTheme(
    val displayName: String,
    val primaryHex: Long,
    val secondaryHex: Long,
    val accentHex: Long,
    val backgroundHex: Long
) {
    CYBER_NEON("Cyber Neon", 0xFFFF007A, 0xFF00E5FF, 0xFFFFD600, 0xFF0A0A14),
    SUNSET_HORIZON("Sunset Horizon", 0xFFFF5722, 0xFFFF9800, 0xFFFFEB3B, 0xFF1A0A14),
    MIDNIGHT_NEBULA("Midnight Nebula", 0xFF8A2BE2, 0xFF4A00E0, 0xFF00FFFF, 0xFF09061A),
    EMERALD_MATRIX("Emerald Matrix", 0xFF00E676, 0xFF00B0FF, 0xFF76FF03, 0xFF04120B),
    VOLCANIC_CRIMSON("Volcanic Crimson", 0xFFFF1744, 0xFFFF5252, 0xFFFFAB00, 0xFF140507),
    CLEAN_SLATE("Clean Slate", 0xFF64748B, 0xFF38BDF8, 0xFFF8FAFC, 0xFF0F172A)
}

enum class CaptionStyle(
    val displayName: String,
    val textColorHex: Long,
    val strokeColorHex: Long,
    val highlightColorHex: Long,
    val uppercase: Boolean
) {
    HORMOZI_YELLOW("Hormozi Yellow", 0xFFFFFFFF, 0xFF000000, 0xFFFFEA00, true),
    MRBEAST_POP("MrBeast Pop", 0xFFFFFFFF, 0xFF000000, 0xFF00E5FF, true),
    EMERALD_PUNCH("Neon Green", 0xFFFFFFFF, 0xFF000000, 0xFF00FF66, true),
    SUNSET_FIRE("Sunset Orange", 0xFFFFFFFF, 0xFF000000, 0xFFFF6D00, true),
    MINIMAL_WHITE("Minimalist Box", 0xFFFFFFFF, 0xFF1E293B, 0xFF94A3B8, false)
}

enum class CaptionAnimType(val displayName: String) {
    WORD_KARAOKE("Word Karaoke"),
    SENTENCE_BOUNCE("Sentence Bounce"),
    GLOW_PULSE("Glow Pulse")
}

data class CaptionWord(
    val word: String,
    val startSec: Float,
    val endSec: Float
)

data class CaptionSegment(
    val index: Int,
    val fullText: String,
    val startSec: Float,
    val endSec: Float,
    val words: List<CaptionWord> = emptyList()
)

data class ShortVideo(
    val id: String,
    val title: String,
    val topic: String,
    val hook: String,
    val body: String,
    val cta: String,
    val script: String,
    val tone: ScriptTone,
    val targetDurationSec: Int = 30,
    val theme: VisualTheme = VisualTheme.CYBER_NEON,
    val captionStyle: CaptionStyle = CaptionStyle.HORMOZI_YELLOW,
    val captionAnim: CaptionAnimType = CaptionAnimType.WORD_KARAOKE,
    val speechRate: Float = 1.1f,
    val speechPitch: Float = 1.0f,
    val createdAt: Long = System.currentTimeMillis(),
    val wordCount: Int = 0,
    val estimatedDurationSec: Float = 0f,
    val tags: List<String> = emptyList(),
    val segments: List<CaptionSegment> = emptyList(),
    val isExported: Boolean = false
)
