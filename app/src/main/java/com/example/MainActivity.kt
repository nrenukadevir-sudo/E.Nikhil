package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- Tab Representation ---
enum class AppTab {
    PLAY, SHORTS, THUMBNAIL
}

class MainActivity : ComponentActivity() {
    private val viewModel: QuizViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        // We will render our own beautiful BottomNavigationBar that adjusts for system safe inset paddings.
                    }
                ) { innerPadding ->
                    MainScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: QuizViewModel, modifier: Modifier = Modifier) {
    var currentTab by remember { mutableStateOf(AppTab.PLAY) }
    val userProgress by viewModel.userProgress.collectAsStateWithLifecycle()
    val isMuted by viewModel.isMutedState.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PolishBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Elegant Header Area
            HeaderBar(
                userProgress = userProgress,
                isMuted = isMuted,
                onMuteToggle = { viewModel.toggleMute() }
            )

            // Screen Content based on current selected tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (currentTab) {
                    AppTab.PLAY -> PlayHubView(viewModel)
                    AppTab.SHORTS -> ShortsCreatorView(viewModel)
                    AppTab.THUMBNAIL -> ThumbnailArchitectView(viewModel)
                }
            }

            // Bottom Navigation Area
            BottomNavBar(currentTab = currentTab, onTabSelect = { currentTab = it })
        }
    }
}

// --- COMMON HEADER COMPONENT ---

@Composable
fun HeaderBar(
    userProgress: UserProgress,
    isMuted: Boolean,
    onMuteToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Level and Title section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(PolishAccentLavender, CircleShape)
                        .shadow(4.dp, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${userProgress.level}",
                        color = PolishAccentDeepPurple,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text(
                        text = "LEVEL",
                        color = PolishTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (userProgress.level >= 12) "Grandmaster" else "Challenger",
                        color = PolishTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Coins & Sound toggles based on the theme design
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Coins Pill
                Row(
                    modifier = Modifier
                        .background(PolishSurfaceInactive, RoundedCornerShape(24.dp))
                        .border(1.dp, PolishBorder, RoundedCornerShape(24.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "$",
                        color = Color(0xFFFFCC00),
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${userProgress.coins}",
                        color = PolishTextPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Volume / Settings Mute Toggle
                IconButton(
                    onClick = onMuteToggle,
                    modifier = Modifier
                        .size(40.dp)
                        .background(PolishSurfaceInactive, CircleShape)
                        .border(1.dp, PolishBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                        contentDescription = "Mute Toggle",
                        tint = if (isMuted) Color.Gray else PolishAccentLavender,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Horizontal Progress Bar
        val xpValue = (userProgress.xp % 100).toFloat() / 100f
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(PolishBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(xpValue)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(PolishAccentLavender, PolishAccentDeepPurple)
                        ),
                        CircleShape
                    )
            )
        }
    }
}

// --- MAIN HUB VIEW (INTERACTIVE PLAY MODE) ---

@Composable
fun PlayHubView(viewModel: QuizViewModel) {
    val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = sessionState) {
            is QuizSessionState.Idle -> PlayHubDashboard(onStartQuiz = { cat -> viewModel.startQuizSession(cat) })
            is QuizSessionState.Loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFFFF007F))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Initializing Live Arena...", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            is QuizSessionState.Active -> ActiveGameplayScreen(state = state, viewModel = viewModel)
            is QuizSessionState.Completed -> GameFinishedScreen(state = state, onDismiss = { viewModel.exitToDashboard() })
        }
    }
}

@Composable
fun PlayHubDashboard(onStartQuiz: (String) -> Unit) {
    val categories = listOf(
        QuizCategoryItem("GK", "General Knowledge & Logic", Icons.Default.Landscape, Color(0xFF00E5FF), "Master daily trivia & worldly brainteasers!"),
        QuizCategoryItem("Movies", "Box Office & Blockbusters", Icons.Default.Movie, Color(0xFFFF007F), "A cinematic race through Hollywood hits & stars."),
        QuizCategoryItem("Gaming", "True Battle Royale Arena", Icons.Default.VideogameAsset, Color(0xFF00FF66), "Max action testing: Free Fire, PUBG, and Esports!"),
        QuizCategoryItem("Sports", "Esports, Soccer & Champions", Icons.Default.SportsBasketball, Color(0xFFFFAB00), "Fast trivia pacing through epic athletic legends."),
        QuizCategoryItem("Science", "Space, Chemistry & Tech", Icons.Default.QueryStats, Color(0xFF7F00FF), "Unearth cosmic physics & biology breakthroughs.")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "CHOOSE YOUR LIVE ARENA",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = PolishTextPrimary,
            letterSpacing = 1.sp
        )
        Text(
            text = "Answer rapidly to climb levels, earn coins and XP rewards!",
            fontSize = 12.sp,
            color = PolishTextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(categories) { cat ->
                CategoryCard(item = cat, onClick = { onStartQuiz(cat.name) })
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        // Quick help card - Styled following "Professional Polish" guidelines
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = PolishAccentDeepPurple.copy(alpha = 0.2f)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, PolishChipBorder)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Tips",
                    tint = PolishAccentLavender,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Pro Tip: Custom quizzes saved in your Creator Studio are automatically mixed into these play categories!",
                    fontSize = 11.sp,
                    color = PolishTextSecondary
                )
            }
        }
    }
}

data class QuizCategoryItem(
    val name: String,
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val description: String
)

@Composable
fun CategoryCard(item: QuizCategoryItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("category_card_${item.name.lowercase()}")
            .clickable { onClick() }
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = PolishSurfaceInactive
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, PolishBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(PolishAccentDeepPurple.copy(alpha = 0.3f), CircleShape)
                    .border(1.dp, PolishChipBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.name,
                    tint = PolishAccentLavender,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    color = PolishTextPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.description,
                    color = PolishTextSecondary,
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = PolishAccentLavender,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// --- ACTIVE GAMEPLAY SCREEN (WITH HIGH ACTION STYLING) ---

@Composable
fun ActiveGameplayScreen(
    state: QuizSessionState.Active,
    viewModel: QuizViewModel
) {
    val q = state.question
    val isAnswered = state.selectedOption != null

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Action Navigation bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.exitToDashboard() },
                    modifier = Modifier
                        .size(36.dp)
                        .background(PolishSurfaceInactive, CircleShape)
                        .border(1.dp, PolishBorder, CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Exit", tint = PolishTextPrimary)
                }

                // Question index progress indicator
                Card(
                    colors = CardDefaults.cardColors(containerColor = PolishAccentDeepPurple),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, PolishChipBorder)
                ) {
                    Text(
                        text = "QUESTION ${state.index + 1} OF ${state.total}",
                        color = PolishAccentLavender,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                // Spacer just to balance columns
                Spacer(modifier = Modifier.size(36.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pulse Timer bar
            TimerStatusRow(timeMax = 15, timeCurrent = state.timeRemaining, isAnswered = isAnswered)

            Spacer(modifier = Modifier.height(16.dp))

            // Main Question Card with Neon Borders
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.2f),
                colors = CardDefaults.cardColors(containerColor = PolishSurfaceInactive),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, PolishBorder)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Category hint chip
                        Box(
                            modifier = Modifier
                                .background(PolishAccentDeepPurple, RoundedCornerShape(12.dp))
                                .border(1.dp, PolishChipBorder, RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = q.category.uppercase(),
                                color = PolishAccentLavender,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Large readable question text
                        Text(
                            text = q.text,
                            color = PolishTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            lineHeight = 26.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Options container (A, B, C, D)
            Column(
                modifier = Modifier.weight(1.8f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val options = listOf(
                    OptionConfig("A", q.optionA),
                    OptionConfig("B", q.optionB),
                    OptionConfig("C", q.optionC),
                    OptionConfig("D", q.optionD)
                )

                for (opt in options) {
                    val isThisSelected = state.selectedOption == opt.key
                    val isThisCorrect = q.correctOption == opt.key

                    // Deciding colors based on state
                    val cardBg: Color
                    val borderCl: Color
                    val textCl: Color

                    if (isAnswered) {
                        if (isThisCorrect) {
                            // Highlighting correct choice green
                            cardBg = Color(0xFF0D331A)
                            borderCl = Color(0xFF00FF66)
                            textCl = Color(0xFF00FF66)
                        } else if (isThisSelected) {
                            // Wrong selected choice red
                            cardBg = Color(0xFF421520)
                            borderCl = Color(0xFFFF3B30)
                            textCl = Color(0xFFFF3B30)
                        } else {
                            // Dimmed non-selected options
                            cardBg = PolishBackground
                            borderCl = PolishBorder.copy(alpha = 0.5f)
                            textCl = PolishTextSecondary.copy(alpha = 0.4f)
                        }
                    } else {
                        // Normal active style
                        cardBg = PolishSurfaceInactive
                        borderCl = PolishBorder
                        textCl = PolishTextPrimary
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("game_option_${opt.key.lowercase()}")
                            .clickable(enabled = !isAnswered) { viewModel.submitAnswer(opt.key) },
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, borderCl)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Option Initial Dot
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        if (isAnswered && isThisCorrect) Color(0xFF00FF66).copy(alpha = 0.2f)
                                        else if (isAnswered && isThisSelected) Color(0xFFFF3B30).copy(alpha = 0.2f)
                                        else PolishBorder,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = opt.key,
                                    color = textCl,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            // Option Content Text
                            Text(
                                text = opt.text,
                                color = textCl,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f)
                            )
                            // State Icon Indicators
                            if (isAnswered) {
                                if (isThisCorrect) {
                                    Icon(Icons.Default.Check, "Correct", tint = Color(0xFF00FF66), modifier = Modifier.size(20.dp))
                                } else if (isThisSelected) {
                                    Icon(Icons.Default.Clear, "Incorrect", tint = Color(0xFFFF3B30), modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Post-answer explanations and Continue buttons
            AnimatedVisibility(
                visible = isAnswered,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Quick informative banner
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (state.isCorrect) Color(0xFF092914) else Color(0xFF2B101D)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (state.isCorrect) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = "Feedback",
                                    tint = if (state.isCorrect) Color(0xFF00FF66) else Color(0xFFFF3B30),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (state.isCorrect) "EXCELLENT! +15 Stars & +10 XP" else "NOPE! Correct Answer was option ${q.correctOption}",
                                    color = if (state.isCorrect) Color(0xFF00FF66) else Color(0xFFFF3B30),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = q.explanation,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.advanceToNextQuestion() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("continue_quiz_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = PolishAccentDeepPurple, contentColor = PolishAccentLavender),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.5.dp, PolishChipBorder)
                    ) {
                        Text("CONTINUE FLIGHT ➔", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

data class OptionConfig(val key: String, val text: String)

@Composable
fun TimerStatusRow(timeMax: Int, timeCurrent: Int, isAnswered: Boolean) {
    val sizeFr = timeCurrent.toFloat() / timeMax.toFloat()
    val progressColor = when {
        sizeFr > 0.6f -> Color(0xFF00E5FF)
        sizeFr > 0.3f -> Color(0xFFFFAB00)
        else -> Color(0xFFFF007F)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Alarm,
            contentDescription = "Timer",
            tint = progressColor,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .clip(CircleShape)
                .background(Color(0xFF26204E))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(if (isAnswered) 1f else sizeFr)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(progressColor, progressColor.copy(alpha = 0.5f))
                        )
                    )
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "${timeCurrent}s",
            color = progressColor,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.width(28.dp),
            textAlign = TextAlign.End
        )
    }
}

// --- GAME FINISHED CELEBRATION MODAL ---

@Composable
fun GameFinishedScreen(state: QuizSessionState.Completed, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .border(2.dp, PolishBorder, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = PolishSurfaceInactive),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Trophy Crown visual with custom Compose Canvas
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(PolishAccentDeepPurple, CircleShape)
                        .border(1.dp, PolishChipBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Trophy",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(46.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "ARENA CLEARED!",
                    color = PolishTextPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "You scored ${state.finalScore} / ${state.totalQuestions} correctly",
                    color = PolishTextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stats row (Coins / XP rewards indicators)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = PolishBackground),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, PolishBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("STARS WON", color = PolishTextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, "Coins", tint = Color.Yellow, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+${state.coinsWon}", color = Color.Yellow, fontWeight = FontWeight.Black, fontSize = 16.sp)
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = PolishBackground),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, PolishBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("XP GAINED", color = PolishTextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("+${state.xpWon}", color = PolishAccentLavender, fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                    }
                }

                if (state.rankUpgrade) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PolishAccentLavender),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, PolishChipBorder)
                    ) {
                        Text(
                            text = "👑 LEVEL UPGRADED! 👑",
                            color = PolishAccentDeepPurple,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PolishAccentDeepPurple, contentColor = PolishAccentLavender),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.5.dp, PolishChipBorder)
                ) {
                    Text("CLAIM REWARDS & LEAVE", fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

// --- SHORTS VIDEO CREATOR STUDIO ---

@Composable
fun ShortsCreatorView(viewModel: QuizViewModel) {
    val customQuestions by viewModel.customQuestions.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val generationError by viewModel.generationError.collectAsStateWithLifecycle()

    var customTopicText by remember { mutableStateOf("") }
    var generatorThemeSelection by remember { mutableStateOf("General") } // "General", "Gaming", "Telugu"

    // Mock Shorts Simulator Engine States
    var isSimulating by remember { mutableStateOf(false) }
    var simulateFrame by remember { mutableStateOf(0) } // 0: Intro hook, 1: Countdown ticking, 2: Reveal, 3: Comment CTA
    var simCountdown by remember { mutableStateOf(5) }
    val coroutineScope = rememberCoroutineScope()

    // Loaded active simulation question
    val presetQuestionsForSim = PresetQuizzes.questions
    val availableQuestionsForSim = (customQuestions.map {
        PresetQuestion(it.question, it.optionA, it.optionB, it.optionC, it.optionD, it.correctOption, "Created with QuizCraft API!", it.category, it.theme)
    } + presetQuestionsForSim).filter { it.theme.equals(generatorThemeSelection, ignoreCase = true) }

    val activeSimQuestion = remember(generatorThemeSelection, customQuestions) {
        availableQuestionsForSim.firstOrNull() ?: PresetQuizzes.questions.first()
    }

    // Dynamic color schemas for Simulator Mockups based on channel styles
    val colors = when (generatorThemeSelection) {
        "Gaming" -> SimulatorColors(
            primary = Color(0xFF00FFCC), // neon mint
            secondary = Color(0xFFFF007F), // hot pink
            textTitle = "BATTLE ARENA SNIPER QUIZ 🎮",
            hookIntro = "ONLY 1% HEROES PASS THIS!",
            ctaAlert = "COMMENT YOUR SCORE & BOOYAH!"
        )
        "Telugu" -> SimulatorColors(
            primary = Color(0xFFFFD700), // royal telugu gold
            secondary = Color(0xFFFF5722), // glowing saffron
            textTitle = "మహారాజు బుద్ధి పరీక్ష క్విజ్! 🌸",
            hookIntro = "మీరు జీనియస్ అయితే అన్ని చెప్పండి!",
            ctaAlert = "మీ స్కోర్ కామెంట్‌లో తెలియజేయండి!"
        )
        else -> SimulatorColors(
            primary = Color(0xFF00FFFF), // dynamic cyan
            secondary = Color(0xFF7F00FF), // cosmic violet
            textTitle = "GENERAL BRAIN SPEED QUIZ ⚡",
            hookIntro = "CAN YOU GET ALL CORRECT?",
            ctaAlert = "COMMENT YOUR SCORE & SUBSCRIBE!"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Topic introduction
        Text(
            text = "9:16 VERTICAL SHORTS STUDIO",
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
        Text(
            text = "Rehearse shorts, generate dynamic quiz tracks using Gemini AI!",
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Large 9:16 vertical player mockup inside row with side options (mocking YouTube Shorts player exactly)
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Main 9:16 mock player skeleton
            Card(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(9f / 16f)
                    .border(2.dp, colors.primary.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF080612)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Cosmic or gaming visual overlay graphics depending on the theme
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        if (generatorThemeSelection == "Gaming") {
                            // Gaming HUD decorations: subtle neon laser matrices
                            drawRect(
                                color = Color(0xFF00FFCC).copy(alpha = 0.05f),
                                size = size
                            )
                        } else {
                            // Subtle radial gradient center
                        }
                    }

                    // Content screens depending on player simulation progress
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Title/Header indicators
                        Text(
                            text = colors.textTitle,
                            color = colors.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                .padding(vertical = 4.dp)
                        )

                        // Central Dynamic view elements
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            when (simulateFrame) {
                                0 -> {
                                    // Step 0: Hook Intro Overlay
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "🚀 CRITICAL HIT:",
                                            color = Color.Yellow,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = colors.hookIntro,
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 15.sp,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )
                                        Spacer(modifier = Modifier.height(20.dp))
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = colors.secondary.copy(alpha = 0.2f)),
                                            border = BorderStroke(1.dp, colors.secondary)
                                        ) {
                                            Text(
                                                text = "Tap Play To Simulate!",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                                1 -> {
                                    // Step 1: Countdown ticking
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = activeSimQuestion.question,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Black,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(20.dp))
                                        // Circular Timer showingCountdown state
                                        Box(
                                            modifier = Modifier
                                                .size(76.dp)
                                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                                .border(2.dp, colors.primary, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "$simCountdown",
                                                color = Color.White,
                                                fontSize = 28.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(text = "Pre-revealing in...", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                                    }
                                }
                                2 -> {
                                    // Step 2: Answer Revealed with Glowing explosion text styling!
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = activeSimQuestion.question,
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontSize = 11.sp,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        // Visual ABCD list with correct highlighted
                                        val optionsList = listOf(
                                            "A" to activeSimQuestion.optionA,
                                            "B" to activeSimQuestion.optionB,
                                            "C" to activeSimQuestion.optionC,
                                            "D" to activeSimQuestion.optionD
                                        )
                                        optionsList.forEach { (k, v) ->
                                            val isCorrect = k.equals(activeSimQuestion.correctOption, ignoreCase = true)
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 3.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (isCorrect) colors.primary.copy(alpha = 0.2f) else Color(0xFF130F25)
                                                ),
                                                shape = RoundedCornerShape(8.dp),
                                                border = BorderStroke(1.5.dp, if (isCorrect) colors.primary else Color(0xFF231B4B))
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(10.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(20.dp)
                                                            .background(if (isCorrect) colors.primary else Color(0xFF2C2454), CircleShape),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(k, color = if (isCorrect) Color.Black else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(v, color = Color.White, fontSize = 10.sp, maxLines = 1)
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Card(
                                             colors = CardDefaults.cardColors(containerColor = colors.primary),
                                             border = BorderStroke(1.dp, Color.White),
                                             shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = "🎉 ANSWER REVEALED 🎉",
                                                color = Color.Black,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 11.sp,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                                3 -> {
                                    // Step 3: End CTA Screen
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "🏁 STAGE COMPLETE 🏁",
                                            color = colors.primary,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = colors.ctaAlert,
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 14.sp,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(24.dp))
                                        // Visual analytics projection
                                        Row(
                                            modifier = Modifier
                                                .background(Color(0xFF251A4D), RoundedCornerShape(8.dp))
                                                .padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.TrendingUp, "Virality", tint = Color(0xFF00FF66), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("ESTIMATED VIRALITY SCORE: +380%", color = Color(0xFF00FF66), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // Bottom channel information banner
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(28.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(colors.secondary, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "@viralQuizBuilder",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Simulator Play controllers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    if (!isSimulating) {
                        isSimulating = true
                        simulateFrame = 0
                        coroutineScope.launch {
                            // Step 0 -> Step 1
                            delay(1800)
                            simulateFrame = 1
                            simCountdown = 5
                            viewModel.soundPlayer.playTick()
                            while (simCountdown > 1) {
                                delay(1000)
                                simCountdown--
                                viewModel.soundPlayer.playTick()
                            }
                            delay(1000)
                            // Step 1 -> Step 2
                            simulateFrame = 2
                            viewModel.soundPlayer.playCorrect()
                            delay(3500)
                            // Step 2 -> Step 3
                            simulateFrame = 3
                            viewModel.soundPlayer.playCoins()
                            delay(3000)
                            // Finish and reset
                            simulateFrame = 0
                            isSimulating = false
                        }
                    }
                },
                enabled = !isSimulating,
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1.3f)
                    .padding(horizontal = 4.dp)
                    .testTag("simulate_play_button")
            ) {
                Text(
                    text = if (isSimulating) "PLAYING SIMULATION..." else "▶ PLAY SHORTS PREVIEW",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }

            // Quick theme select options in a row
            val themesList = listOf("General" to "⚡ Gen", "Gaming" to "🎮 Game", "Telugu" to "🌸 Tel")
            Row(modifier = Modifier.weight(1.5f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                themesList.forEach { (tKey, label) ->
                    val isSel = generatorThemeSelection == tKey
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("theme_select_$tKey")
                            .clickable { generatorThemeSelection = tKey },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSel) colors.primary else Color(0xFF1E173C)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                            Text(
                                text = label,
                                color = if (isSel) Color.Black else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // AI Quiz generator prompt area
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF140F2D).copy(alpha = 0.9f)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF2B225E))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "🤖 AI GEMINI WRITING ASSISTANT",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Text(
                    text = "Enter any topic to trigger custom question writeups instantly into your template channels!",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = customTopicText,
                        onValueChange = { customTopicText = it },
                        placeholder = { Text("Topic e.g. Free Fire Guns, Bollywood, Space...", fontSize = 11.sp) },
                        modifier = Modifier
                            .weight(1.2f)
                            .height(50.dp)
                            .testTag("ai_topic_input"),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF0F0024),
                            unfocusedContainerColor = Color(0xFF0F0024),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedIndicatorColor = colors.primary
                        ),
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                        maxLines = 1,
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (customTopicText.isNotBlank()) {
                                viewModel.triggerGeminiGeneration(customTopicText, generatorThemeSelection)
                                customTopicText = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007F)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(0.7f)
                            .height(50.dp)
                            .testTag("ai_generate_button"),
                        enabled = !isGenerating
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("AI WRITE", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }

                if (generationError != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Notice: ${generationError}",
                        color = Color(0xFFFFAB00),
                        fontSize = 10.sp,
                        lineHeight = 12.sp
                    )
                }
            }
        }
    }
}

data class SimulatorColors(
    val primary: Color,
    val secondary: Color,
    val textTitle: String,
    val hookIntro: String,
    val ctaAlert: String
)

// --- INDIVIDUAL CLICKBAIT THUMBNAIL STUDIO ---

@Composable
fun ThumbnailArchitectView(viewModel: QuizViewModel) {
    var clickbaitText by remember { mutableStateOf("IMPOSSIBLE QUIZ") }
    var subtitleText by remember { mutableStateOf("99% WILL FAIL!") }
    
    // Architect controls
    var themeColorIndex by remember { mutableStateOf(0) } // 0: Laser Cyan, 1: Shock Saturated Saffron, 2: Cyber Gold Metallic
    var hasShockedFace by remember { mutableStateOf(true) }
    var hasGlowingQuestionMarks by remember { mutableStateOf(true) }

    val themes = listOf(
        ThumbnailThemeConfig(Color(0xFF00FFCC), Color(0xFFFF007F)),
        ThumbnailThemeConfig(Color(0xFFFF5722), Color(0xFFE91E63)),
        ThumbnailThemeConfig(Color(0xFFFFD700), Color(0xFF00E5FF))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Topic intro
        Text(
            text = "YOUTUBE THUMBNAIL DESIGNER",
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            color = PolishTextPrimary
        )
        Text(
            text = "Elevate click-through-rates using shocking visual combinations!",
            fontSize = 11.sp,
            color = PolishTextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Main live thumbnail rendering canvas
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .shadow(8.dp, RoundedCornerShape(16.dp))
                .border(2.dp, themes[themeColorIndex].primary, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Background image - loading the custom generated asset
                Image(
                    painter = painterResource(id = R.drawable.ic_viral_thumbnail),
                    contentDescription = "Viral Thumbnail background",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Neon overlay brushes
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                            radius = size.width
                        )
                    )
                }

                // If overlays are checked, draw shocking emojis or indicators on top!
                if (hasShockedFace) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 16.dp)
                            .size(90.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(themes[themeColorIndex].secondary.copy(alpha = 0.3f), Color.Transparent)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "😱",
                            fontSize = 62.sp,
                            modifier = Modifier.rotate(8f)
                        )
                    }
                }

                if (hasGlowingQuestionMarks) {
                    // Top corner glowing question mark
                    Box(modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)) {
                        Text(
                            text = "❓",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.rotate(-15f)
                        )
                    }
                }

                // Neon Headline overlay blocks
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 16.dp)
                        .fillMaxWidth(0.65f)
                ) {
                    Box(
                        modifier = Modifier
                            .background(themes[themeColorIndex].primary, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = clickbaitText.uppercase(),
                            color = Color.Black,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                            .border(1.dp, themes[themeColorIndex].secondary, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = subtitleText.uppercase(),
                            color = themes[themeColorIndex].secondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Architect modification parameters
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = PolishSurfaceInactive),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, PolishBorder)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "🛠 DESIGNER PARAMETERS",
                        color = PolishTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                // Clickbait core headline
                item {
                    OutlinedTextField(
                        value = clickbaitText,
                        onValueChange = { clickbaitText = it },
                        label = { Text("Core Clickbait Heading") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("thumbnail_heading_input"),
                        textStyle = TextStyle(color = PolishTextPrimary, fontSize = 12.sp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PolishTextPrimary,
                            unfocusedTextColor = PolishTextSecondary,
                            focusedBorderColor = themes[themeColorIndex].primary,
                            unfocusedLabelColor = PolishTextSecondary.copy(alpha = 0.5f),
                            focusedLabelColor = themes[themeColorIndex].primary
                        )
                    )
                }

                // Clickbait subtitle
                item {
                    OutlinedTextField(
                        value = subtitleText,
                        onValueChange = { subtitleText = it },
                        label = { Text("Sub-heading CTA tag") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("thumbnail_subtitle_input"),
                        textStyle = TextStyle(color = PolishTextPrimary, fontSize = 12.sp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PolishTextPrimary,
                            unfocusedTextColor = PolishTextSecondary,
                            focusedBorderColor = themes[themeColorIndex].secondary,
                            unfocusedLabelColor = PolishTextSecondary.copy(alpha = 0.5f),
                            focusedLabelColor = themes[themeColorIndex].secondary
                        )
                    )
                }

                // Switch states selection
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = hasShockedFace,
                                onCheckedChange = { hasShockedFace = it },
                                colors = CheckboxDefaults.colors(checkedColor = themes[themeColorIndex].primary),
                                modifier = Modifier.size(36.dp).testTag("shocked_face_checkbox")
                            )
                            Text("Shocked Reaction Face emoji", color = PolishTextPrimary, fontSize = 11.sp)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = hasGlowingQuestionMarks,
                                onCheckedChange = { hasGlowingQuestionMarks = it },
                                colors = CheckboxDefaults.colors(checkedColor = themes[themeColorIndex].primary),
                                modifier = Modifier.size(36.dp).testTag("question_marks_checkbox")
                            )
                            Text("Neon Glow Q's", color = PolishTextPrimary, fontSize = 11.sp)
                        }
                    }
                }

                // Theme switch selector
                item {
                    Column {
                        Text(
                            text = "Select Neon Blast Theme Color Color Scheme:",
                            color = PolishTextSecondary,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val colorNames = listOf("Laser Cyan (Mint)", "Toxic Sunrise (Saffron)", "Cyber Neon Gold")
                            themes.forEachIndexed { idx, t ->
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("color_theme_select_$idx")
                                        .clickable { themeColorIndex = idx },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (themeColorIndex == idx) t.primary else PolishBackground
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, if (themeColorIndex == idx) t.primary else PolishBorder)
                                ) {
                                    Text(
                                        text = colorNames[idx],
                                        color = if (themeColorIndex == idx) PolishAccentDeepPurple else PolishTextSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .fillMaxWidth(),
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                // Viral Forecast Card
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C2413)),
                        border = BorderStroke(1.dp, Color(0xFF00FF66))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.TrendingUp, "Score indicator", tint = Color(0xFF00FF66), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Estimated CTR Boost: +32.4% (EXTREMELY HIGH VIRAL POTENTIAL)",
                                    color = Color(0xFF00FF66),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = "Using high-impact headings alongside reaction assets guarantees higher engagement feeds.",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class ThumbnailThemeConfig(val primary: Color, val secondary: Color)

// --- BOTTOM NAVIGATION BAR (POLISHED FOR GRADIENT THEMES) ---

@Composable
fun BottomNavBar(
    currentTab: AppTab,
    onTabSelect: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = PolishSurfaceInactive),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, PolishBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabsInfo = listOf(
                AppTabInfo(AppTab.PLAY, "Game Arena", Icons.Default.SportsEsports, "play_tab_button"),
                AppTabInfo(AppTab.SHORTS, "Shorts Studio", Icons.Default.MovieFilter, "shorts_tab_button"),
                AppTabInfo(AppTab.THUMBNAIL, "Thumb Architect", Icons.Default.InsertPhoto, "thumbnail_tab_button")
            )

            for (tab in tabsInfo) {
                val isSel = currentTab == tab.tab
                val tabColor = if (isSel) PolishAccentLavender else PolishTextSecondary.copy(alpha = 0.6f)

                Column(
                    modifier = Modifier
                        .testTag(tab.tag)
                        .clickable { onTabSelect(tab.tab) }
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = tabColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tab.label,
                        color = tabColor,
                        fontSize = 11.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

data class AppTabInfo(
    val tab: AppTab,
    val label: String,
    val icon: ImageVector,
    val tag: String
)

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
