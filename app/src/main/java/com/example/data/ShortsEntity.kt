package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.CaptionAnimType
import com.example.model.CaptionSegment
import com.example.model.CaptionStyle
import com.example.model.CaptionWord
import com.example.model.ScriptTone
import com.example.model.ShortVideo
import com.example.model.VisualTheme
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "shorts_projects")
data class ShortsEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val topic: String,
    val hook: String,
    val body: String,
    val cta: String,
    val script: String,
    val tone: String,
    val targetDurationSec: Int,
    val theme: String,
    val captionStyle: String,
    val captionAnim: String,
    val speechRate: Float,
    val speechPitch: Float,
    val createdAt: Long,
    val wordCount: Int,
    val estimatedDurationSec: Float,
    val tagsJson: String,
    val segmentsJson: String,
    val isExported: Boolean
) {
    fun toDomain(): ShortVideo {
        val parsedTone = runCatching { ScriptTone.valueOf(tone) }.getOrDefault(ScriptTone.VIRAL_PUNCHY)
        val parsedTheme = runCatching { VisualTheme.valueOf(theme) }.getOrDefault(VisualTheme.CYBER_NEON)
        val parsedCapStyle = runCatching { CaptionStyle.valueOf(captionStyle) }.getOrDefault(CaptionStyle.HORMOZI_YELLOW)
        val parsedCapAnim = runCatching { CaptionAnimType.valueOf(captionAnim) }.getOrDefault(CaptionAnimType.WORD_KARAOKE)

        val tagsList = mutableListOf<String>()
        runCatching {
            val jsonArray = JSONArray(tagsJson)
            for (i in 0 until jsonArray.length()) {
                tagsList.add(jsonArray.getString(i))
            }
        }

        val segmentsList = mutableListOf<CaptionSegment>()
        runCatching {
            val jsonArray = JSONArray(segmentsJson)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val wordsArray = obj.optJSONArray("words") ?: JSONArray()
                val wordList = mutableListOf<CaptionWord>()
                for (w in 0 until wordsArray.length()) {
                    val wObj = wordsArray.getJSONObject(w)
                    wordList.add(
                        CaptionWord(
                            word = wObj.getString("word"),
                            startSec = wObj.getDouble("startSec").toFloat(),
                            endSec = wObj.getDouble("endSec").toFloat()
                        )
                    )
                }
                segmentsList.add(
                    CaptionSegment(
                        index = obj.getInt("index"),
                        fullText = obj.getString("fullText"),
                        startSec = obj.getDouble("startSec").toFloat(),
                        endSec = obj.getDouble("endSec").toFloat(),
                        words = wordList
                    )
                )
            }
        }

        return ShortVideo(
            id = id,
            title = title,
            topic = topic,
            hook = hook,
            body = body,
            cta = cta,
            script = script,
            tone = parsedTone,
            targetDurationSec = targetDurationSec,
            theme = parsedTheme,
            captionStyle = parsedCapStyle,
            captionAnim = parsedCapAnim,
            speechRate = speechRate,
            speechPitch = speechPitch,
            createdAt = createdAt,
            wordCount = wordCount,
            estimatedDurationSec = estimatedDurationSec,
            tags = tagsList,
            segments = segmentsList,
            isExported = isExported
        )
    }

    companion object {
        fun fromDomain(video: ShortVideo): ShortsEntity {
            val tagsArr = JSONArray()
            video.tags.forEach { tagsArr.put(it) }

            val segsArr = JSONArray()
            video.segments.forEach { seg ->
                val obj = JSONObject()
                obj.put("index", seg.index)
                obj.put("fullText", seg.fullText)
                obj.put("startSec", seg.startSec.toDouble())
                obj.put("endSec", seg.endSec.toDouble())
                val wordsArr = JSONArray()
                seg.words.forEach { w ->
                    val wObj = JSONObject()
                    wObj.put("word", w.word)
                    wObj.put("startSec", w.startSec.toDouble())
                    wObj.put("endSec", w.endSec.toDouble())
                    wordsArr.put(wObj)
                }
                obj.put("words", wordsArr)
                segsArr.put(obj)
            }

            return ShortsEntity(
                id = video.id,
                title = video.title,
                topic = video.topic,
                hook = video.hook,
                body = video.body,
                cta = video.cta,
                script = video.script,
                tone = video.tone.name,
                targetDurationSec = video.targetDurationSec,
                theme = video.theme.name,
                captionStyle = video.captionStyle.name,
                captionAnim = video.captionAnim.name,
                speechRate = video.speechRate,
                speechPitch = video.speechPitch,
                createdAt = video.createdAt,
                wordCount = video.wordCount,
                estimatedDurationSec = video.estimatedDurationSec,
                tagsJson = tagsArr.toString(),
                segmentsJson = segsArr.toString(),
                isExported = video.isExported
            )
        }
    }
}
