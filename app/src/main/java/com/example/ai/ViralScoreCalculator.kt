package com.example.ai

data class ViralAnalysis(
    val score: Int, // 0 - 100
    val hookRating: String, // "God-tier Hook", "High Retention", "Decent", "Needs Curiosity Gap"
    val pacingScore: Int,
    val wordsPerMinute: Int,
    val gradeLevel: String,
    val feedbackTips: List<String>
)

object ViralScoreCalculator {

    private val VIRAL_HOOK_TRIGGERS = listOf(
        "stop scrolling", "you won't believe", "what if", "scientists just",
        "the truth about", "never do this", "3 things", "nobody talks about",
        "secret", "illegal", "wait until", "terrifying", "warning",
        "the 1%", "psychological trick", "did you know", "most people think",
        "how to", "rule number 1", "if you ever"
    )

    private val VIRAL_CTA_TRIGGERS = listOf(
        "subscribe", "follow", "comment below", "share this", "save this",
        "drop a", "which one are you", "link in bio"
    )

    fun analyze(hook: String, body: String, cta: String, durationSec: Float): ViralAnalysis {
        var points = 60
        val tips = mutableListOf<String>()

        val hookLower = hook.lowercase()
        val ctaLower = cta.lowercase()

        // 1. Hook evaluation
        val hookWords = hook.split(Regex("\\s+")).filter { it.isNotBlank() }
        val matchedHookKeywords = VIRAL_HOOK_TRIGGERS.count { hookLower.contains(it) }

        if (hookWords.size in 4..16) {
            points += 15
        } else if (hookWords.size > 20) {
            points -= 10
            tips.add("Hook is too wordy. Keep your first 2 seconds under 12 punchy words.")
        }

        if (matchedHookKeywords > 0) {
            points += 15
        } else {
            tips.add("Add a curiosity gap or emotional trigger in the hook (e.g., 'Stop scrolling', 'What if').")
        }

        // 2. CTA evaluation
        val hasCta = VIRAL_CTA_TRIGGERS.any { ctaLower.contains(it) }
        if (hasCta) {
            points += 10
        } else {
            tips.add("Add a clear Call to Action at the end to boost engagement & follows.")
        }

        // 3. Pacing & WPM
        val fullText = "$hook $body $cta"
        val totalWords = fullText.split(Regex("\\s+")).count { it.isNotBlank() }
        val minutes = (durationSec / 60f).coerceAtLeast(0.1f)
        val wpm = (totalWords / minutes).toInt()

        val pacingScore = when (wpm) {
            in 140..175 -> {
                points += 10
                95
            }
            in 120..140 -> 82
            in 175..200 -> 85
            else -> 70
        }

        val hookRating = when {
            matchedHookKeywords >= 2 || (matchedHookKeywords == 1 && hookWords.size <= 12) -> "🔥 God-tier Viral Hook"
            matchedHookKeywords == 1 -> "⚡ Strong Retention Hook"
            hookWords.size <= 14 -> "👍 Punchy Opener"
            else -> "⚠️ Needs Stronger Curiosity Gap"
        }

        val finalScore = points.coerceIn(40, 99)

        return ViralAnalysis(
            score = finalScore,
            hookRating = hookRating,
            pacingScore = pacingScore,
            wordsPerMinute = wpm,
            gradeLevel = "Grade 6-7 (Spoken Conversational)",
            feedbackTips = tips
        )
    }
}
