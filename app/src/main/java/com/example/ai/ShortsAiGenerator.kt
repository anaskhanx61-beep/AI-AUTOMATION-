package com.example.ai

import com.example.model.CaptionSegment
import com.example.model.CaptionStyle
import com.example.model.ScriptTone
import com.example.model.ShortVideo
import com.example.model.VisualTheme
import com.example.tts.TtsNarrationEngine
import java.util.UUID

object ShortsAiGenerator {

    suspend fun generateShort(
        topic: String,
        tone: ScriptTone = ScriptTone.VIRAL_PUNCHY,
        targetDurationSec: Int = 30,
        selectedTheme: VisualTheme = VisualTheme.CYBER_NEON,
        selectedCaptionStyle: CaptionStyle = CaptionStyle.HORMOZI_YELLOW,
        speechRate: Float = 1.1f
    ): ShortVideo {
        // Attempt Gemini API first if configured
        val geminiResult = if (GeminiShortsService.isKeyConfigured()) {
            GeminiShortsService.generateShortsScript(topic, tone.displayName, targetDurationSec)
        } else {
            null
        }

        val generated = geminiResult ?: generateBuiltInScript(topic, tone, targetDurationSec)

        val fullScript = "${generated.hook} ${generated.body} ${generated.cta}".trim()
        val (segments, estimatedDuration) = TtsNarrationEngine.buildCaptionTimeline(fullScript, speechRate)
        val wordCount = fullScript.split(Regex("\\s+")).count { it.isNotBlank() }

        return ShortVideo(
            id = UUID.randomUUID().toString(),
            title = generated.title,
            topic = topic,
            hook = generated.hook,
            body = generated.body,
            cta = generated.cta,
            script = fullScript,
            tone = tone,
            targetDurationSec = targetDurationSec,
            theme = selectedTheme,
            captionStyle = selectedCaptionStyle,
            speechRate = speechRate,
            speechPitch = 1.0f,
            wordCount = wordCount,
            estimatedDurationSec = estimatedDuration,
            tags = generated.tags,
            segments = segments,
            isExported = false
        )
    }

    private fun generateBuiltInScript(
        topic: String,
        tone: ScriptTone,
        targetDurationSec: Int
    ): ScriptGenerationResult {
        val topicLower = topic.lowercase().trim()

        // Match niche or use adaptive synthesis
        return when {
            topicLower.contains("psycholog") || topicLower.contains("mind") || topicLower.contains("brain") -> {
                buildPsychologyScript(topic, tone)
            }
            topicLower.contains("money") || topicLower.contains("rich") || topicLower.contains("wealth") || topicLower.contains("invest") -> {
                buildWealthScript(topic, tone)
            }
            topicLower.contains("space") || topicLower.contains("universe") || topicLower.contains("planet") || topicLower.contains("alien") -> {
                buildSpaceScript(topic, tone)
            }
            topicLower.contains("ai") || topicLower.contains("tech") || topicLower.contains("robot") || topicLower.contains("future") -> {
                buildTechScript(topic, tone)
            }
            topicLower.contains("habit") || topicLower.contains("focus") || topicLower.contains("dopamine") || topicLower.contains("sleep") -> {
                buildProductivityScript(topic, tone)
            }
            topicLower.contains("history") || topicLower.contains("ancient") || topicLower.contains("dark") || topicLower.contains("secret") -> {
                buildHistoryScript(topic, tone)
            }
            else -> {
                buildAdaptiveUniversalScript(topic, tone, targetDurationSec)
            }
        }
    }

    private fun buildPsychologyScript(topic: String, tone: ScriptTone): ScriptGenerationResult {
        return ScriptGenerationResult(
            title = "3 Dark Psychology Tricks You Must Know 🧠",
            hook = "Stop scrolling if you want to know if someone is lying straight to your face.",
            body = "First, observe their blinking pattern. When someone fabricates a story, their blink rate drops suddenly, then skyrockets right after they finish. Second, if you pause and stay silent after they answer, their subconscious discomfort will force them to fill the silence with the truth. Most people never notice this until it's too late.",
            cta = "Drop a comment if you've seen this happen, and subscribe for more psychological power.",
            tags = listOf("#PsychologyTricks", "#MindHacks", "#BodyLanguage", "#Shorts", "#ViralFacts")
        )
    }

    private fun buildWealthScript(topic: String, tone: ScriptTone): ScriptGenerationResult {
        return ScriptGenerationResult(
            title = "The 1% Rule That Keeps People Poor 💰",
            hook = "The wealthiest 1% know this rule about money, but the other 99% completely ignore it.",
            body = "Poor people buy liabilities that look like assets to impress strangers they don't even like. The ultra wealthy buy cash-flowing assets that quietly fund their lifestyle while they sleep. If your income stops today, how many days could you survive on your savings? That exact number is your true wealth, not your watch or your car.",
            cta = "Subscribe if you're building real wealth this year, and share this with an ambitious friend.",
            tags = listOf("#MoneyMindset", "#WealthBuilding", "#FinancialFreedom", "#HustleShorts", "#Millionaire")
        )
    }

    private fun buildSpaceScript(topic: String, tone: ScriptTone): ScriptGenerationResult {
        return ScriptGenerationResult(
            title = "Why Space is Absolutely Terrifying 🪐",
            hook = "Scientists just discovered something terrifying drifting through deep space.",
            body = "There are rogue black holes floating through the darkness at three million miles per hour completely invisible to the naked eye. If one passed through our outer solar system, planetary orbits would shatter instantly before we could even detect its gravity. The vacuum of space isn't just empty—it's waiting.",
            cta = "Hit subscribe if cosmic mysteries give you chills, and tell me your theory below.",
            tags = listOf("#SpaceFacts", "#Cosmos", "#BlackHole", "#NASA", "#MindBlowing")
        )
    }

    private fun buildTechScript(topic: String, tone: ScriptTone): ScriptGenerationResult {
        return ScriptGenerationResult(
            title = "The AI Breakthrough Nobody Saw Coming ⚡",
            hook = "What if I told you the world as we know it changed forever this morning?",
            body = "Engineers just achieved autonomous neural reasoning that doesn't just predict the next word—it actually tests hypotheses in real time. Within twelve months, creative workflows that once required five teams of specialists will run in milliseconds on your smartphone. We aren't waiting for the future anymore; we are living inside it.",
            cta = "Are you excited or terrified? Comment below and follow for cutting-edge tech updates.",
            tags = listOf("#ArtificialIntelligence", "#TechTrends", "#FutureTech", "#AIRevolution", "#Shorts")
        )
    }

    private fun buildProductivityScript(topic: String, tone: ScriptTone): ScriptGenerationResult {
        return ScriptGenerationResult(
            title = "How To 10x Your Focus In 48 Hours 🎯",
            hook = "If you can't focus for more than 10 minutes, your brain isn't broken—it's overstimulated.",
            body = "Every notification floods your brain with cheap dopamine spikes, destroying your deep work capacity. Try this protocol tomorrow: do a 24-hour dopamine fast. No short videos, no social scrolling, no snacking while working. When you strip away false stimulation, boring hard tasks suddenly become deeply satisfying.",
            cta = "Save this video for tomorrow morning and subscribe to reclaim your superhuman focus.",
            tags = listOf("#FocusHacks", "#DopamineDetox", "#Productivity", "#SelfImprovement", "#Motivation")
        )
    }

    private fun buildHistoryScript(topic: String, tone: ScriptTone): ScriptGenerationResult {
        return ScriptGenerationResult(
            title = "The Darkest Coincidence in Human History 📜",
            hook = "You won't believe the chilling historical coincidence school textbooks never taught you.",
            body = "In 1858, a shipwrecked traveler was rescued off the coast of England by a total stranger. Decades later, that same rescued man's grandson was pulled from a freezing river by another stranger. When they checked the historical records, both rescuers were father and son from the exact same family line. Coincidence, or is destiny real?",
            cta = "What do you think? Comment your theory below and hit follow for bizarre true history.",
            tags = listOf("#HistoryUncovered", "#Mysteries", "#TrueStories", "#CrazyFacts", "#Shorts")
        )
    }

    private fun buildAdaptiveUniversalScript(
        topic: String,
        tone: ScriptTone,
        targetDurationSec: Int
    ): ScriptGenerationResult {
        val cleanTopic = topic.trim().replaceFirstChar { it.uppercase() }

        val hook = when (tone) {
            ScriptTone.VIRAL_PUNCHY -> "Stop scrolling right now, because what you're about to hear about $cleanTopic will completely change your perspective."
            ScriptTone.DRAMATIC_MYSTERY -> "There is a hidden truth about $cleanTopic that almost nobody in the world is talking about."
            ScriptTone.HUSTLE_MOTIVATION -> "If you want to master $cleanTopic, you must abandon how 99% of people think about it."
            ScriptTone.EDUCATIONAL_DEEP -> "Scientists and experts recently uncovered a mind-blowing revelation regarding $cleanTopic."
            ScriptTone.COMEDY_WITTY -> "The hilarious thing about $cleanTopic is that we all pretend to understand it, but nobody actually does."
            ScriptTone.SCIFI_FUTURE -> "Imagine waking up ten years from now, and $cleanTopic has transformed every single aspect of civilization."
        }

        val body = when (tone) {
            ScriptTone.VIRAL_PUNCHY -> "Here is what happens behind the scenes. Most people assume $cleanTopic is straightforward, but the underlying mechanics are radically different. When you look closer, the actual patterns defy common logic. The moment you understand this mechanism, you gain an unfair competitive advantage that puts you ahead of everyone else."
            ScriptTone.DRAMATIC_MYSTERY -> "For decades, the real reality behind $cleanTopic was kept quiet. Key clues were overlooked until researchers connected the missing links. Now the undeniable evidence proves that what seemed impossible is actually happening right before our eyes."
            ScriptTone.HUSTLE_MOTIVATION -> "Success with $cleanTopic comes down to radical consistency. While average people look for shortcuts and easy dopamine, true winners embrace the grind and master the fundamental principles. Execute daily without excuses, and watch your results compound exponentially."
            ScriptTone.EDUCATIONAL_DEEP -> "The core principle of $cleanTopic lies in how complex systems interact. Research demonstrates that small adjustments create cascading breakthrough effects. When these dynamics align, extraordinary breakthroughs occur that reshape our entire understanding."
            ScriptTone.COMEDY_WITTY -> "Think about it: we spend hours debating $cleanTopic, only to realize the simplest answer was right in front of us the entire time. It's ridiculous, it's brilliant, and honestly, we should have known better all along."
            ScriptTone.SCIFI_FUTURE -> "Technological acceleration in $cleanTopic is surpassing every prediction. What was once confined to visionary speculation is becoming real hardware and real intelligence. The boundary between imagination and reality is disappearing."
        }

        val cta = when (tone) {
            ScriptTone.VIRAL_PUNCHY -> "Hit that subscribe button for daily mind-bending insights, and share this with someone who needs to see it."
            ScriptTone.DRAMATIC_MYSTERY -> "What do you think is really going on? Drop your craziest theory in the comments and subscribe for part two."
            ScriptTone.HUSTLE_MOTIVATION -> "Drop a 100 in the comments if you're executing today, and follow for daily high-performance mindset."
            ScriptTone.EDUCATIONAL_DEEP -> "Subscribe to expand your mind every single day, and leave a like if you learned something new."
            ScriptTone.COMEDY_WITTY -> "Tag a friend who does this every single day and follow for more relatable reality checks."
            ScriptTone.SCIFI_FUTURE -> "Would you survive this future? Let me know below and subscribe for deep future tech dispatches."
        }

        val title = "$cleanTopic: The Unspoken Truth 🔥"

        return ScriptGenerationResult(
            title = title,
            hook = hook,
            body = body,
            cta = cta,
            tags = listOf("#Shorts", "#Viral", "#Trending", "#${cleanTopic.replace(" ", "")}")
        )
    }

    /**
     * Starter default projects so the user has immediate videos to play and test!
     */
    fun createDefaultStarterShorts(): List<ShortVideo> {
        val short1 = generateBuiltInScript("Psychology tricks", ScriptTone.VIRAL_PUNCHY, 30)
        val short2 = generateBuiltInScript("Why space is terrifying", ScriptTone.DRAMATIC_MYSTERY, 30)
        val short3 = generateBuiltInScript("The 1% money rule", ScriptTone.HUSTLE_MOTIVATION, 30)

        fun createShort(result: ScriptGenerationResult, topic: String, tone: ScriptTone, theme: VisualTheme, capStyle: CaptionStyle): ShortVideo {
            val script = "${result.hook} ${result.body} ${result.cta}"
            val (segs, dur) = TtsNarrationEngine.buildCaptionTimeline(script, 1.1f)
            return ShortVideo(
                id = UUID.randomUUID().toString(),
                title = result.title,
                topic = topic,
                hook = result.hook,
                body = result.body,
                cta = result.cta,
                script = script,
                tone = tone,
                targetDurationSec = 30,
                theme = theme,
                captionStyle = capStyle,
                speechRate = 1.1f,
                speechPitch = 1.0f,
                wordCount = script.split(Regex("\\s+")).count { it.isNotBlank() },
                estimatedDurationSec = dur,
                tags = result.tags,
                segments = segs
            )
        }

        return listOf(
            createShort(short1, "3 Dark Psychology Tricks", ScriptTone.VIRAL_PUNCHY, VisualTheme.CYBER_NEON, CaptionStyle.HORMOZI_YELLOW),
            createShort(short2, "Why Space is Terrifying", ScriptTone.DRAMATIC_MYSTERY, VisualTheme.MIDNIGHT_NEBULA, CaptionStyle.MRBEAST_POP),
            createShort(short3, "The 1% Wealth Rule", ScriptTone.HUSTLE_MOTIVATION, VisualTheme.SUNSET_HORIZON, CaptionStyle.SUNSET_FIRE)
        )
    }
}
