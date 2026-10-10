package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CaptionAnimType
import com.example.model.CaptionStyle
import com.example.model.VisualTheme
import com.example.ui.ShortsViewModel
import com.example.ui.components.ShortsCanvasPlayer
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.AccentYellow
import com.example.ui.theme.BrandRed
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlayerScreen(
    viewModel: ShortsViewModel,
    onNavigateToCreate: () -> Unit,
    onNavigateToExport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentVideo by viewModel.currentVideo.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentTimeSec by viewModel.currentTimeSec.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()

    var activeTabSub by remember { mutableStateOf(0) } // 0=Themes, 1=Captions, 2=Voice

    if (currentVideo == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No Short Selected", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onNavigateToCreate,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
                ) {
                    Text("Create a Short First")
                }
            }
        }
        return
    }

    val video = currentVideo!!

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val availableHeight = maxHeight
        // Allocate space so 9:16 player fits smoothly alongside customizer controls
        val playerHeight = (availableHeight * 0.58f).coerceIn(320.dp, 580.dp)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }

            // 9:16 Vertical Video Player
            item {
                Box(
                    modifier = Modifier
                        .height(playerHeight)
                        .aspectRatio(9f / 16f)
                        .testTag("shorts_player_container"),
                    contentAlignment = Alignment.Center
                ) {
                    ShortsCanvasPlayer(
                        video = video,
                        isPlaying = isPlaying,
                        currentTimeSec = currentTimeSec,
                        isMuted = isMuted,
                        onTogglePlay = { viewModel.togglePlayPause() },
                        onSeek = { viewModel.seekTo(it) },
                        onToggleMute = { viewModel.toggleMute() },
                        onReplay = { viewModel.replay() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Quick Action Buttons (Edit Script / Export Assets)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 480.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateToCreate,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit Script", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = onNavigateToExport,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("export_assets_nav_button")
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export Assets", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Customizer Tabs Card (Themes / Captions / Voice)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 500.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Sub Tabs Selector
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkSurface, RoundedCornerShape(12.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf(
                                Triple(0, "Visual Theme", Icons.Default.Palette),
                                Triple(1, "Captions", Icons.Default.Subtitles),
                                Triple(2, "Voice Audio", Icons.Default.RecordVoiceOver)
                            ).forEach { (idx, label, icon) ->
                                val isSel = activeTabSub == idx
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSel) BrandRed else Color.Transparent,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { activeTabSub = idx }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = if (isSel) Color.White else TextSecondary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = label,
                                            color = if (isSel) Color.White else TextSecondary,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        when (activeTabSub) {
                            0 -> {
                                // Theme selector
                                Text("Select 9:16 Canvas Theme", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    VisualTheme.values().forEach { thm ->
                                        val isSel = video.theme == thm
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isSel) DarkSurfaceCard else DarkSurface,
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.5.dp,
                                                if (isSel) BrandRed else DarkBorder
                                            ),
                                            modifier = Modifier.clickable { viewModel.updateTheme(thm) }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(14.dp)
                                                        .background(Color(thm.primaryHex), CircleShape)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = thm.displayName,
                                                    color = if (isSel) Color.White else TextSecondary,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            1 -> {
                                // Captions Customizer
                                Text("Caption Typography Style", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CaptionStyle.values().forEach { cStyle ->
                                        val isSel = video.captionStyle == cStyle
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isSel) DarkSurfaceCard else DarkSurface,
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.5.dp,
                                                if (isSel) NeonCyan else DarkBorder
                                            ),
                                            modifier = Modifier.clickable { viewModel.updateCaptionStyle(cStyle) }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(12.dp)
                                                        .background(Color(cStyle.highlightColorHex), CircleShape)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = cStyle.displayName,
                                                    color = if (isSel) Color.White else TextSecondary,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text("Animation Mode", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    CaptionAnimType.values().forEach { anim ->
                                        val isSel = video.captionAnim == anim
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSel) AccentOrange else DarkSurface,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) AccentOrange else DarkBorder),
                                            modifier = Modifier.clickable { viewModel.updateCaptionAnim(anim) }
                                        ) {
                                            Text(
                                                text = anim.displayName,
                                                color = if (isSel) Color.White else TextSecondary,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            2 -> {
                                // Voice & Narration Speed
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Pacing / Speech Rate", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = String.format(Locale.US, "%.1fx", video.speechRate),
                                        color = AccentYellow,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Slider(
                                    value = video.speechRate,
                                    onValueChange = { viewModel.updateSpeechRate(it) },
                                    valueRange = 0.8f..1.5f,
                                    steps = 6,
                                    colors = SliderDefaults.colors(
                                        thumbColor = BrandRed,
                                        activeTrackColor = BrandRed,
                                        inactiveTrackColor = DarkBorder
                                    )
                                )
                                Text(
                                    text = "💡 1.1x is standard for YouTube Shorts to maintain high audience retention.",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
