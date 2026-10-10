package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ai.ViralScoreCalculator
import com.example.tts.TtsNarrationEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("ShortsCraft AI", appName)
    }

    @Test
    fun `caption timeline builder creates timed segments`() {
        val script = "Stop scrolling right now. This psychology trick will blow your mind. Subscribe for more."
        val (segments, duration) = TtsNarrationEngine.buildCaptionTimeline(script, 1.1f)

        assertEquals(3, segments.size)
        assertTrue(duration > 0f)
        assertTrue(segments[0].words.isNotEmpty())
    }

    @Test
    fun `viral score calculator gives high score for strong hook`() {
        val hook = "Stop scrolling if you want to know the truth."
        val body = "Scientists just discovered something terrifying in deep space. Rogue black holes are drifting toward us."
        val cta = "Subscribe for more cosmic mysteries."

        val analysis = ViralScoreCalculator.analyze(hook, body, cta, 25f)
        assertTrue(analysis.score >= 70)
        assertTrue(analysis.hookRating.contains("Viral") || analysis.hookRating.contains("Retention") || analysis.hookRating.contains("Opener"))
    }
}
