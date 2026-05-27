package com.example.ui

import android.app.Application
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Vibrator
import android.os.VibrationEffect
import android.os.Build
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Audio Synthesizer for Retro Game Sound Effects using native ToneGenerator
 */
class QuizSoundPlayer(context: Context) {
    private val attributionContext = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        context.createAttributionContext("attributionTag")
    } else {
        context
    }
    private val lock = Any()
    private var toneGenerator: ToneGenerator? = null
    var isMuted: Boolean = false

    init {
        synchronized(lock) {
            try {
                toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    fun playCorrect() {
        if (isMuted) return
        viewModelScopePlay {
            synchronized(lock) {
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 250)
                } catch (e: Throwable) {
                    // fallbacks
                }
            }
        }
    }

    fun playWrong() {
        if (isMuted) return
        viewModelScopePlay {
            synchronized(lock) {
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 350)
                } catch (e: Throwable) {
                    // fallbacks
                }
            }
        }
    }

    fun playTick() {
        if (isMuted) return
        viewModelScopePlay {
            synchronized(lock) {
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
                } catch (e: Throwable) {
                    // fallbacks
                }
            }
        }
    }

    fun playCoins() {
        if (isMuted) return
        viewModelScopePlay {
            try {
                synchronized(lock) {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 150)
                }
                delay(120)
                synchronized(lock) {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 150)
                }
            } catch (e: Throwable) {
                // fallbacks
            }
        }
    }

    private fun viewModelScopePlay(block: suspend () -> Unit) {
        // Simple manual launch to handle delay
        Thread {
            try {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND)
                kotlinx.coroutines.runBlocking { block() }
            } catch (e: Throwable){}
        }.start()
    }

    fun release() {
        synchronized(lock) {
            try {
                toneGenerator?.release()
            } catch (e: Throwable){}
            toneGenerator = null
        }
    }
}

// Ensure the vibrator triggers nice feedback on answers
class QuizHapticFeedback(context: Context) {
    private val attributionContext = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        context.createAttributionContext("attributionTag")
    } else {
        context
    }
    private val vibrator = attributionContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

    fun vibrateSuccess() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(100)
            }
        } catch (e: Throwable){}
    }

    fun vibrateFail() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val pattern = longArrayOf(0, 80, 80, 120)
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 80, 80, 120), -1)
            }
        } catch (e: Throwable){}
    }
}

// --- View State Representations ---

sealed interface QuizSessionState {
    object Idle : QuizSessionState
    object Loading : QuizSessionState
    data class Active(
        val question: GameQuestion,
        val index: Int,
        val total: Int,
        val timeRemaining: Int,
        val selectedOption: String? = null, // "A","B","C","D" (null if unanswered)
        val showFeedback: Boolean = false,
        val isCorrect: Boolean = false,
        val coinsEarned: Int = 0,
        val xpEarned: Int = 0
    ) : QuizSessionState
    data class Completed(
        val finalScore: Int,
        val totalQuestions: Int,
        val coinsWon: Int,
        val xpWon: Int,
        val rankUpgrade: Boolean = false
    ) : QuizSessionState
}

data class GameQuestion(
    val text: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctOption: String, // "A","B","C","D"
    val explanation: String,
    val category: String,
    val theme: String = "General"
)

class QuizViewModel(application: Application) : AndroidViewModel(application) {

    private val db = androidx.room.Room.databaseBuilder(
        application,
        QuizDatabase::class.java,
        "quiz_craft_db"
    ).fallbackToDestructiveMigration().build()

    private val repository = QuizRepository(db.quizDao)
    val soundPlayer = QuizSoundPlayer(application)
    val haptic = QuizHapticFeedback(application)

    // --- State Observables ---

    private val _userProgress = MutableStateFlow(UserProgress())
    val userProgress: StateFlow<UserProgress> = _userProgress.asStateFlow()

    private val _customQuestions = MutableStateFlow<List<CustomQuizQuestion>>(emptyList())
    val customQuestions: StateFlow<List<CustomQuizQuestion>> = _customQuestions.asStateFlow()

    private val _sessionState = MutableStateFlow<QuizSessionState>(QuizSessionState.Idle)
    val sessionState: StateFlow<QuizSessionState> = _sessionState.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _generationError = MutableStateFlow<String?>(null)
    val generationError: StateFlow<String?> = _generationError.asStateFlow()

    // --- Auxiliary UI States ---
    val isMutedState = MutableStateFlow(false)

    // --- Local Gameplay Properties ---
    private var activeQuestionsList = listOf<GameQuestion>()
    private var currentQuestionIndex = 0
    private var timerJob: Job? = null
    private var answeredCountInSession = 0
    private var correctCountInSession = 0
    private var coinsInSession = 0
    private var xpInSession = 0

    init {
        // Synchronize Room values to state flows
        viewModelScope.launch {
            repository.userProgress.collectLatest { progress ->
                if (progress != null) {
                    _userProgress.value = progress
                } else {
                    // Initialize first-time profile in Room
                    val initial = UserProgress(id = 1, level = 1, xp = 0, coins = 100, totalPlayed = 0, totalCorrect = 0, highScore = 0)
                    repository.updateProgress(initial)
                    _userProgress.value = initial
                }
            }
        }
        viewModelScope.launch {
            repository.customQuestions.collectLatest { list ->
                _customQuestions.value = list
            }
        }
    }

    // --- Audio control ---
    fun toggleMute() {
        val current = !soundPlayer.isMuted
        soundPlayer.isMuted = current
        isMutedState.value = current
    }

    // --- Interactive Gameplay Engine ---

    fun startQuizSession(category: String) {
        viewModelScope.launch {
            _sessionState.value = QuizSessionState.Loading
            delay(400) // smooth immersive transition delay

            // Combine custom questions matching category, and presets
            val customMatched = _customQuestions.value
                .filter { it.category.equals(category, ignoreCase = true) }
                .map {
                    GameQuestion(
                        text = it.question,
                        optionA = it.optionA,
                        optionB = it.optionB,
                        optionC = it.optionC,
                        optionD = it.optionD,
                        correctOption = it.correctOption,
                        explanation = "User Created Question!",
                        category = it.category,
                        theme = it.theme
                    )
                }

            val presetMatched = PresetQuizzes.questions
                .filter { it.category.equals(category, ignoreCase = true) }
                .map {
                    GameQuestion(
                        text = it.question,
                        optionA = it.optionA,
                        optionB = it.optionB,
                        optionC = it.optionC,
                        optionD = it.optionD,
                        correctOption = it.correctOption,
                        explanation = it.explanation,
                        category = it.category,
                        theme = it.theme
                    )
                }

            // Shuffle and cap at 5 questions for quick gaming sessions
            val combined = (customMatched + presetMatched).shuffled()
            activeQuestionsList = if (combined.isEmpty()) {
                // Fallback to all presets if empty
                PresetQuizzes.questions.shuffled().take(5).map {
                    GameQuestion(it.question, it.optionA, it.optionB, it.optionC, it.optionD, it.correctOption, it.explanation, it.category, it.theme)
                }
            } else {
                combined.take(5)
            }

            currentQuestionIndex = 0
            answeredCountInSession = 0
            correctCountInSession = 0
            coinsInSession = 0
            xpInSession = 0

            presentCurrentQuestion()
        }
    }

    private fun presentCurrentQuestion() {
        if (currentQuestionIndex >= activeQuestionsList.size) {
            finishSession()
            return
        }

        val question = activeQuestionsList[currentQuestionIndex]
        _sessionState.value = QuizSessionState.Active(
            question = question,
            index = currentQuestionIndex,
            total = activeQuestionsList.size,
            timeRemaining = 15 // 15 seconds per question for maximum adrenaline
        )

        startTimerEngine()
    }

    private fun startTimerEngine() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = _sessionState.value
                if (current is QuizSessionState.Active) {
                    if (current.selectedOption != null) {
                        // Question already answered, skip timing ticking down
                        break
                    }
                    if (current.timeRemaining <= 1) {
                        // Run out of time! Mark as wrong automatically
                        soundPlayer.playWrong()
                        haptic.vibrateFail()
                        _sessionState.value = current.copy(
                            timeRemaining = 0,
                            selectedOption = "", // empty means timed out
                            showFeedback = true,
                            isCorrect = false
                        )
                        break
                    } else {
                        // Tick sound
                        if (current.timeRemaining <= 5) {
                            soundPlayer.playTick()
                        }
                        _sessionState.value = current.copy(timeRemaining = current.timeRemaining - 1)
                    }
                } else {
                    break
                }
            }
        }
    }

    fun submitAnswer(option: String) {
        val state = _sessionState.value
        if (state !is QuizSessionState.Active || state.selectedOption != null) return

        timerJob?.cancel()
        val isCorrect = option.equals(state.question.correctOption, ignoreCase = true)
        
        val coinsWon = if (isCorrect) 15 else 2
        val xpWon = if (isCorrect) 10 else 0

        coinsInSession += coinsWon
        xpInSession += xpWon
        answeredCountInSession++
        if (isCorrect) {
            correctCountInSession++
            soundPlayer.playCorrect()
            haptic.vibrateSuccess()
        } else {
            soundPlayer.playWrong()
            haptic.vibrateFail()
        }

        _sessionState.value = state.copy(
            selectedOption = option,
            showFeedback = true,
            isCorrect = isCorrect,
            coinsEarned = coinsWon,
            xpEarned = xpWon
        )
    }

    fun advanceToNextQuestion() {
        timerJob?.cancel()
        currentQuestionIndex++
        presentCurrentQuestion()
    }

    private fun finishSession() {
        timerJob?.cancel()
        
        viewModelScope.launch {
            _sessionState.value = QuizSessionState.Loading
            delay(500)

            val progress = _userProgress.value
            val currentXP = progress.xp + xpInSession
            val currentCoins = progress.coins + coinsInSession
            
            // Retro mobile levels calculated at 100 XP increments
            val calcLevel = (currentXP / 100) + 1
            val levelUpgrad = calcLevel > progress.level

            val updatedHighScore = if (correctCountInSession > progress.highScore) correctCountInSession else progress.highScore

            val updatedProgress = UserProgress(
                id = 1,
                level = calcLevel,
                xp = currentXP,
                coins = currentCoins,
                totalPlayed = progress.totalPlayed + 1,
                totalCorrect = progress.totalCorrect + correctCountInSession,
                highScore = updatedHighScore
            )

            repository.updateProgress(updatedProgress)
            _userProgress.value = updatedProgress

            if (coinsInSession > 0) {
                soundPlayer.playCoins()
            }

            _sessionState.value = QuizSessionState.Completed(
                finalScore = correctCountInSession,
                totalQuestions = activeQuestionsList.size,
                coinsWon = coinsInSession,
                xpWon = xpInSession,
                rankUpgrade = levelUpgrad
            )
        }
    }

    fun exitToDashboard() {
        timerJob?.cancel()
        _sessionState.value = QuizSessionState.Idle
    }

    // --- Custom Quiz Creation and Gemini AI Generation ---

    fun triggerGeminiGeneration(topic: String, theme: String = "General") {
        if (topic.trim().isEmpty()) return
        
        viewModelScope.launch {
            _isGenerating.value = true
            _generationError.value = null
            try {
                val questions = withContext(Dispatchers.IO) {
                    val lang = if (theme == "Telugu") "Telugu (తెలుగు)" else "English"
                    GeminiClient.generateQuizQuestions(topicPrompt = topic, count = 5, language = lang)
                }

                if (questions.isEmpty()) {
                    _generationError.value = "Gemini API key is invalid or returned empty results. Generating local offline substitutes!"
                    // Generates 3 local preset questions as creative substitute
                    val localAdditions = PresetQuizzes.questions.shuffled().take(3).map {
                        CustomQuizQuestion(
                            question = "Offline Mode: ${it.question}",
                            optionA = it.optionA,
                            optionB = it.optionB,
                            optionC = it.optionC,
                            optionD = it.optionD,
                            correctOption = it.correctOption,
                            category = if (theme == "Telugu") "Telugu" else "GK",
                            theme = theme
                        )
                    }
                    localAdditions.forEach { repository.addCustomQuestion(it) }
                } else {
                    questions.forEach {
                        repository.addCustomQuestion(
                            CustomQuizQuestion(
                                question = it.question,
                                optionA = it.optionA,
                                optionB = it.optionB,
                                optionC = it.optionC,
                                optionD = it.optionD,
                                correctOption = it.correctOption,
                                category = if (theme == "Telugu") "Telugu" else "GK",
                                theme = theme
                            )
                        )
                    }
                }
            } catch (e: Throwable) {
                _generationError.value = "Failed: ${e.localizedMessage}. Generating local offline substitutes!"
                // Offline fallback logic
                val offlineItems = PresetQuizzes.questions.filter { it.theme == theme }.shuffled().take(3)
                if (offlineItems.isNotEmpty()) {
                    offlineItems.forEach {
                        repository.addCustomQuestion(
                            CustomQuizQuestion(
                                question = "[Offline] " + it.question,
                                optionA = it.optionA,
                                optionB = it.optionB,
                                optionC = it.optionC,
                                optionD = it.optionD,
                                correctOption = it.correctOption,
                                category = it.category,
                                theme = theme
                            )
                        )
                    }
                } else {
                    // Fallback to general presets
                    PresetQuizzes.questions.shuffled().take(2).forEach {
                        repository.addCustomQuestion(
                            CustomQuizQuestion(
                                question = "[Offline fallback] " + it.question,
                                optionA = it.optionA,
                                optionB = it.optionB,
                                optionC = it.optionC,
                                optionD = it.optionD,
                                correctOption = it.correctOption,
                                category = it.category,
                                theme = theme
                            )
                        )
                    }
                }
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun saveManualQuestion(
        text: String,
        a: String,
        b: String,
        c: String,
        d: String,
        answer: String,
        category: String,
        theme: String
    ) {
        viewModelScope.launch {
            repository.addCustomQuestion(
                CustomQuizQuestion(
                    question = text,
                    optionA = a,
                    optionB = b,
                    optionC = c,
                    optionD = d,
                    correctOption = answer,
                    category = category,
                    theme = theme
                )
            )
        }
    }

    fun deleteCustomQuestion(id: Int) {
        viewModelScope.launch {
            repository.deleteQuestion(id)
        }
    }
    
    fun clearCustomQuestions() {
        viewModelScope.launch {
            repository.clearQuestions()
        }
    }

    // --- Purchase dynamic items helper ---
    fun purchasePowerUp(cost: Int): Boolean {
        val current = _userProgress.value
        if (current.coins >= cost) {
            viewModelScope.launch {
                repository.updateProgress(current.copy(coins = current.coins - cost))
            }
            soundPlayer.playCoins()
            return true
        }
        return false
    }

    override fun onCleared() {
        super.onCleared()
        soundPlayer.release()
    }
}
