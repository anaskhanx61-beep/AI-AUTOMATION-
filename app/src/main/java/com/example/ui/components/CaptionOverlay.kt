package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CaptionAnimType
import com.example.model.CaptionSegment
import com.example.model.CaptionStyle

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CaptionOverlay(
    segments: List<CaptionSegment>,
    currentTimeSec: Float,
    captionStyle: CaptionStyle,
    captionAnim: CaptionAnimType,
    modifier: Modifier = Modifier
) {
    // Find current active segment
    val currentSegment = remember(segments, currentTimeSec) {
        segments.firstOrNull { seg ->
            currentTimeSec >= seg.startSec && currentTimeSec <= seg.endSec
        } ?: segments.lastOrNull { seg ->
            currentTimeSec >= seg.endSec
        }?.takeIf { (currentTimeSec - it.endSec) < 1.0f }
    }

    if (currentSegment == null) return

    val textColor = Color(captionStyle.textColorHex)
    val highlightColor = Color(captionStyle.highlightColorHex)
    val strokeColor = Color(captionStyle.strokeColorHex)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = currentSegment.index,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "caption_seg_anim"
        ) { _ ->
            // Container card for readability
            Box(
                modifier = Modifier
                    .shadow(12.dp, RoundedCornerShape(14.dp))
                    .background(
                        Color.Black.copy(alpha = 0.55f),
                        RoundedCornerShape(14.dp)
                    )
                    .border(
                        1.dp,
                        Color.White.copy(alpha = 0.15f),
                        RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                if (captionAnim == CaptionAnimType.WORD_KARAOKE && currentSegment.words.isNotEmpty()) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalArrangement = Arrangement.Center
                    ) {
                        currentSegment.words.forEach { wordObj ->
                            val isWordActive = currentTimeSec >= wordObj.startSec && currentTimeSec < wordObj.endSec
                            val isWordPast = currentTimeSec >= wordObj.endSec

                            val wordColor = when {
                                isWordActive -> highlightColor
                                isWordPast -> textColor
                                else -> textColor.copy(alpha = 0.7f)
                            }

                            val displayText = if (captionStyle.uppercase) {
                                wordObj.word.uppercase()
                            } else {
                                wordObj.word
                            }

                            Text(
                                text = "$displayText ",
                                style = TextStyle(
                                    fontSize = if (isWordActive) 23.sp else 21.sp,
                                    fontWeight = if (isWordActive) FontWeight.Black else FontWeight.ExtraBold,
                                    fontFamily = FontFamily.SansSerif,
                                    color = wordColor,
                                    shadow = Shadow(
                                        color = if (isWordActive) highlightColor.copy(alpha = 0.8f) else strokeColor,
                                        offset = Offset(2f, 2f),
                                        blurRadius = if (isWordActive) 14f else 4f
                                    )
                                ),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp)
                            )
                        }
                    }
                } else {
                    // Whole sentence display
                    val displayText = if (captionStyle.uppercase) {
                        currentSegment.fullText.uppercase()
                    } else {
                        currentSegment.fullText
                    }

                    Text(
                        text = displayText,
                        style = TextStyle(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.SansSerif,
                            color = highlightColor,
                            shadow = Shadow(
                                color = strokeColor,
                                offset = Offset(2f, 2f),
                                blurRadius = 8f
                            )
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
