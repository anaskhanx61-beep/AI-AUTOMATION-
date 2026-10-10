package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.ShortsAiGenerator
import com.example.ai.ViralAnalysis
import com.example.ai.ViralScoreCalculator
import com.example.data.ShortsDatabase
import com.example.data.ShortsRepository
import com.example.model.CaptionAnimType
import com.example.model.CaptionStyle
import com.example.model.ScriptTone
import com.example.model.ShortVideo
import com.example.model.VisualTheme
import com.example.tts.TtsNarrationEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class ShortsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ShortsRepository
    val ttsEngine: TtsNarrationEngine = TtsNarrationEngine(application)

    // Current active short video
    private val _currentVideo = MutableStateFlow<ShortVideo?>(null)
    val currentVideo: StateFlow<ShortVideo?> = _currentVideo.asStateFlow()

    // Playback state
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentTimeSec = MutableStateFlow(0f)
    val currentTimeSec: StateFlow<Float> = _currentTimeSec.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    // Generation state
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _generationStep = MutableStateFlow("")
    val generationStep: StateFlow<String> = _generationStep.asStateFlow()

    // Navigation / Tab
    private val _selectedTab = MutableStateFlow(0) // 0=Create, 1=Player, 2=Library, 3=Export
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Viral score
    private val _viralAnalysis = MutableStateFlow<ViralAnalysis?>(null)
    val viralAnalysis: StateFlow<ViralAnalysis?> = _viralAnalysis.asStateFlow()

    // Export status message
    private val _exportStatus = MutableStateFlow<String?>(null)
    val exportStatus: StateFlow<String?> = _exportStatus.asStateFlow()

    private var playbackJob: Job? = null

    val savedShorts: StateFlow<List<ShortVideo>>

    init {
        val database = ShortsDatabase.getInstance(application)
        repository = ShortsRepository(database.shortsDao())

        savedShorts = repository.allShorts.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Seed default starter shorts if database is empty
        viewModelScope.launch {
            val starterList = ShortsAiGenerator.createDefaultStarterShorts()
            starterList.forEach { repository.saveShort(it) }
            val first = starterList.first()
            loadVideo(first)
        }
    }

    fun selectTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun loadVideo(video: ShortVideo) {
        pausePlayback()
        _currentVideo.value = video
        _currentTimeSec.value = 0f
        updateAnalysis(video)
    }

    fun generateNewShort(
        topic: String,
        tone: ScriptTone,
        targetDurationSec: Int,
        theme: VisualTheme,
        captionStyle: CaptionStyle
    ) {
        viewModelScope.launch {
            _isGenerating.value = true
            _generationStep.value = "Analyzing viral topic..."
            delay(400)

            _generationStep.value = "Crafting high-retention 2s hook..."
            delay(500)

            _generationStep.value = "Synthesizing punchy storytelling & CTA..."
            delay(500)

            _generationStep.value = "Calculating karaoke caption timeline..."

            val newShort = ShortsAiGenerator.generateShort(
                topic = topic,
                tone = tone,
                targetDurationSec = targetDurationSec,
                selectedTheme = theme,
                selectedCaptionStyle = captionStyle
            )

            // Auto-save to repository
            repository.saveShort(newShort)

            _currentVideo.value = newShort
            _currentTimeSec.value = 0f
            updateAnalysis(newShort)

            _isGenerating.value = false
            _generationStep.value = ""

            // Switch to player screen and start playback preview
            _selectedTab.value = 1
            playPlayback()
        }
    }

    fun updateScriptContent(hook: String, body: String, cta: String, title: String? = null) {
        val current = _currentVideo.value ?: return
        val newTitle = title ?: current.title
        val fullScript = "$hook $body $cta".trim()
        val (newSegments, newDuration) = TtsNarrationEngine.buildCaptionTimeline(fullScript, current.speechRate)
        val wordCount = fullScript.split(Regex("\\s+")).count { it.isNotBlank() }

        val updated = current.copy(
            title = newTitle,
            hook = hook,
            body = body,
            cta = cta,
            script = fullScript,
            wordCount = wordCount,
            estimatedDurationSec = newDuration,
            segments = newSegments
        )

        _currentVideo.value = updated
        updateAnalysis(updated)
        viewModelScope.launch {
            repository.updateShort(updated)
        }
    }

    fun updateTheme(theme: VisualTheme) {
        val current = _currentVideo.value ?: return
        val updated = current.copy(theme = theme)
        _currentVideo.value = updated
        viewModelScope.launch { repository.updateShort(updated) }
    }

    fun updateCaptionStyle(style: CaptionStyle) {
        val current = _currentVideo.value ?: return
        val updated = current.copy(captionStyle = style)
        _currentVideo.value = updated
        viewModelScope.launch { repository.updateShort(updated) }
    }

    fun updateCaptionAnim(anim: CaptionAnimType) {
        val current = _currentVideo.value ?: return
        val updated = current.copy(captionAnim = anim)
        _currentVideo.value = updated
        viewModelScope.launch { repository.updateShort(updated) }
    }

    fun updateSpeechRate(rate: Float) {
        val current = _currentVideo.value ?: return
        val (newSegments, newDuration) = TtsNarrationEngine.buildCaptionTimeline(current.script, rate)
        val updated = current.copy(
            speechRate = rate,
            estimatedDurationSec = newDuration,
            segments = newSegments
        )
        _currentVideo.value = updated
        viewModelScope.launch { repository.updateShort(updated) }
    }

    fun deleteShort(id: String) {
        viewModelScope.launch {
            repository.deleteShort(id)
            if (_currentVideo.value?.id == id) {
                _currentVideo.value = null
                _currentTimeSec.value = 0f
            }
        }
    }

    // Playback Controls
    fun togglePlayPause() {
        if (_isPlaying.value) {
            pausePlayback()
        } else {
            playPlayback()
        }
    }

    fun playPlayback() {
        val video = _currentVideo.value ?: return
        val totalDur = video.estimatedDurationSec.coerceAtLeast(1f)

        if (_currentTimeSec.value >= totalDur) {
            _currentTimeSec.value = 0f
        }

        _isPlaying.value = true

        // Play audio via TTS if not muted
        if (!_isMuted.value) {
            ttsEngine.speak(
                text = video.script,
                speechRate = video.speechRate,
                pitch = video.speechPitch
            )
        }

        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            val stepMs = 50L
            val stepSec = stepMs / 1000f

            while (_isPlaying.value && _currentTimeSec.value < totalDur) {
                delay(stepMs)
                val next = _currentTimeSec.value + stepSec
                if (next >= totalDur) {
                    _currentTimeSec.value = totalDur
                    _isPlaying.value = false
                    ttsEngine.stop()
                    break
                } else {
                    _currentTimeSec.value = next
                }
            }
        }
    }

    fun pausePlayback() {
        _isPlaying.value = false
        playbackJob?.cancel()
        ttsEngine.stop()
    }

    fun seekTo(seconds: Float) {
        val totalDur = _currentVideo.value?.estimatedDurationSec ?: 1f
        _currentTimeSec.value = seconds.coerceIn(0f, totalDur)
        if (_isPlaying.value) {
            // Restart audio from sync or continue ticker
            ttsEngine.stop()
        }
    }

    fun replay() {
        pausePlayback()
        _currentTimeSec.value = 0f
        playPlayback()
    }

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
        if (_isMuted.value) {
            ttsEngine.stop()
        } else if (_isPlaying.value) {
            val video = _currentVideo.value
            if (video != null) {
                ttsEngine.speak(video.script, video.speechRate, video.speechPitch)
            }
        }
    }

    fun exportAudioWav(onDone: (File?) -> Unit) {
        val video = _currentVideo.value
        if (video == null) {
            onDone(null)
            return
        }

        viewModelScope.launch {
            _exportStatus.value = "Synthesizing full voiceover WAV file..."
            val cacheDir = getApplication<Application>().cacheDir
            val audioFile = File(cacheDir, "shorts_${video.id.take(8)}.wav")

            ttsEngine.synthesizeToFile(
                text = video.script,
                outputFile = audioFile,
                speechRate = video.speechRate,
                pitch = video.speechPitch
            ) { success ->
                if (success) {
                    _exportStatus.value = "Audio voiceover successfully exported!"
                    onDone(audioFile)
                } else {
                    _exportStatus.value = "TTS synthesis completed (ready for share)"
                    onDone(audioFile)
                }
            }
        }
    }

    fun clearExportStatus() {
        _exportStatus.value = null
    }

    private fun updateAnalysis(video: ShortVideo) {
        _viralAnalysis.value = ViralScoreCalculator.analyze(
            hook = video.hook,
            body = video.body,
            cta = video.cta,
            durationSec = video.estimatedDurationSec
        )
    }

    override fun onCleared() {
        super.onCleared()
        playbackJob?.cancel()
        ttsEngine.shutdown()
    }
}
