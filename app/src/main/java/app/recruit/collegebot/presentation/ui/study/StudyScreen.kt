package app.recruit.collegebot.presentation.ui.study

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import app.recruit.collegebot.domain.model.QuizQuestion
import app.recruit.collegebot.presentation.viewmodel.QuizUiState
import app.recruit.collegebot.presentation.viewmodel.QuizViewModel

private val ExpoOut = Easing { t ->
    if (t == 1f) 1f else 1f - Math.pow(2.0, -10.0 * t).toFloat()
}

private val optionLabels = listOf("A", "B", "C", "D")

private val presetTopics = listOf(
    "Data Structures", "Algorithms", "Operating Systems",
    "DBMS", "Computer Networks", "OOP in Java",
    "Digital Electronics", "Engineering Mathematics",
    "Software Engineering", "Computer Architecture"
)

// ─────────────────────────────────────────────────────────────────────
// Entry point
// ─────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: QuizViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier       = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text  = "Study with AI",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text  = "MCQ Generator",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.reset()
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint               = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    if (uiState is QuizUiState.Active || uiState is QuizUiState.Completed) {
                        IconButton(onClick = { viewModel.reset() }) {
                            Icon(
                                imageVector        = Icons.Default.Refresh,
                                contentDescription = "New quiz",
                                tint               = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        AnimatedContent(
            targetState  = uiState,
            transitionSpec = {
                (fadeIn(tween(300, easing = ExpoOut)) +
                 slideInVertically(tween(300, easing = ExpoOut)) { it / 10 })
                    .togetherWith(fadeOut(tween(200)))
            },
            label = "quiz-phase",
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) { state ->
            when (state) {
                is QuizUiState.Idle    -> TopicInputPhase(
                    onGenerate = { viewModel.generateQuiz(it) }
                )
                is QuizUiState.Loading -> LoadingPhase()
                is QuizUiState.Active  -> QuizPhase(
                    state    = state,
                    onSelect = { viewModel.selectAnswer(it) },
                    onNext   = { viewModel.nextQuestion() }
                )
                is QuizUiState.Completed -> ResultsPhase(
                    state       = state,
                    onRetry     = { viewModel.retryTopic() },
                    onNewTopic  = { viewModel.reset() }
                )
                is QuizUiState.Error -> ErrorPhase(
                    message = state.message,
                    onRetry = { viewModel.reset() }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────
// Phase 1 — Topic input
// ─────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TopicInputPhase(onGenerate: (String) -> Unit) {
    var input by remember { mutableStateOf(TextFieldValue("")) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header card
        Surface(
            modifier       = Modifier.fillMaxWidth(),
            shape          = RoundedCornerShape(20.dp),
            color          = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        ) {
            Column(
                modifier            = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text  = "Generate MCQs",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text      = "Pick a subject or type any topic.\nGemini generates 5 questions instantly.",
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // Custom topic input
        TopicTextField(
            value    = input,
            onChange = { input = it },
            onSubmit = { if (input.text.isNotBlank()) onGenerate(input.text.trim()) }
        )

        Spacer(Modifier.height(16.dp))

        // Generate button
        PrimaryButton(
            text = "Generate 5 Questions",
            enabled = input.text.isNotBlank(),
            onClick = { onGenerate(input.text.trim()) }
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text  = "Or pick a subject",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        // Preset topic chips
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement   = Arrangement.spacedBy(8.dp),
            modifier              = Modifier.fillMaxWidth()
        ) {
            presetTopics.forEach { topic ->
                TopicChip(
                    text    = topic,
                    onClick = { onGenerate(topic) }
                )
            }
        }
    }
}

@Composable
fun TopicTextField(
    value: TextFieldValue,
    onChange: (TextFieldValue) -> Unit,
    onSubmit: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    val borderAlpha by animateFloatAsState(
        targetValue   = if (focused) 1f else 0f,
        animationSpec = tween(180, easing = ExpoOut),
        label         = "topic-border"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = borderAlpha),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        BasicTextField(
            value         = value,
            onValueChange = onChange,
            modifier      = Modifier
                .fillMaxWidth()
                .onFocusChanged { focused = it.isFocused },
            textStyle     = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush   = SolidColor(MaterialTheme.colorScheme.primary),
            singleLine    = true,
            decorationBox = { inner ->
                if (value.text.isEmpty()) {
                    Text(
                        text  = "e.g. Sorting algorithms, Normalisation…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                inner()
            }
        )
    }
}

@Composable
fun PrimaryButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue   = if (isPressed) 0.96f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessHigh),
        label         = "gen-btn-scale"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (enabled) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(
                interactionSource = interactionSource,
                indication        = null,
                enabled           = enabled,
                onClick           = onClick
            )
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text  = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (enabled) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun TopicChip(text: String, onClick: () -> Unit) {
    Surface(
        shape   = RoundedCornerShape(20.dp),
        color   = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Text(
            text     = text,
            style    = MaterialTheme.typography.labelMedium,
            color    = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────
// Phase 2 — Loading
// ─────────────────────────────────────────────────────────────────────

@Composable
fun LoadingPhase() {
    Box(
        modifier         = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier  = Modifier.size(48.dp),
                color     = MaterialTheme.colorScheme.primary,
                strokeCap = StrokeCap.Round
            )
            Text(
                text  = "Generating questions…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────
// Phase 3 — Active quiz
// ─────────────────────────────────────────────────────────────────────

@Composable
fun QuizPhase(
    state: QuizUiState.Active,
    onSelect: (Int) -> Unit,
    onNext: () -> Unit
) {
    val progress by animateFloatAsState(
        targetValue   = (state.currentIndex + 1).toFloat() / state.questions.size,
        animationSpec = tween(400, easing = ExpoOut),
        label         = "quiz-progress"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Progress row
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text  = "Question ${state.currentIndex + 1} of ${state.questions.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text  = state.topic,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            LinearProgressIndicator(
                progress       = progress,
                modifier       = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape),
                color          = MaterialTheme.colorScheme.primary,
                trackColor     = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap      = StrokeCap.Round
            )
        }

        // Question card
        Surface(
            shape          = RoundedCornerShape(20.dp),
            color          = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier       = Modifier.fillMaxWidth()
        ) {
            Text(
                text     = state.currentQuestion.question,
                style    = MaterialTheme.typography.titleMedium,
                color    = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(20.dp)
            )
        }

        // Options
        state.currentQuestion.options.forEachIndexed { index, option ->
            OptionButton(
                label         = optionLabels[index],
                text          = option,
                state         = optionState(index, state),
                enabled       = !state.isAnswered,
                onClick       = { onSelect(index) }
            )
        }

        // Explanation
        AnimatedVisibility(
            visible = state.showExplanation,
            enter   = expandVertically(tween(280, easing = ExpoOut)) + fadeIn(tween(200)),
            exit    = shrinkVertically() + fadeOut()
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text  = "Explanation",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text  = state.currentQuestion.explanation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Next / Finish button
        AnimatedVisibility(
            visible = state.isAnswered,
            enter   = fadeIn(tween(200)) + slideInVertically(tween(200, easing = ExpoOut)) { it / 4 },
            exit    = fadeOut()
        ) {
            PrimaryButton(
                text    = if (state.isLastQuestion) "See Results" else "Next Question",
                enabled = true,
                onClick = onNext
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}

internal enum class OptionState { DEFAULT, CORRECT, WRONG, MISSED }

private fun optionState(index: Int, state: QuizUiState.Active): OptionState {
    if (!state.isAnswered) return OptionState.DEFAULT
    val correct = state.currentQuestion.correctIndex
    return when {
        index == correct && state.selectedIndex == index -> OptionState.CORRECT
        index == correct                                 -> OptionState.MISSED   // show correct when user was wrong
        index == state.selectedIndex                     -> OptionState.WRONG
        else                                             -> OptionState.DEFAULT
    }
}

@Composable
internal fun OptionButton(
    label: String,
    text: String,
    state: OptionState,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val (bgColor, borderColor, labelBg, textColor) = when (state) {
        OptionState.CORRECT -> Tuple4(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.onSurface
        )
        OptionState.WRONG -> Tuple4(
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
            MaterialTheme.colorScheme.error,
            MaterialTheme.colorScheme.error,
            MaterialTheme.colorScheme.onSurface
        )
        OptionState.MISSED -> Tuple4(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        OptionState.DEFAULT -> Tuple4(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurface
        )
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue   = if (isPressed && enabled) 0.97f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessHigh),
        label         = "option-scale-$label"
    )

    Row(
        modifier = Modifier
            .scale(scale)
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(
                width = 1.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication        = null,
                enabled           = enabled,
                onClick           = onClick
            )
            .padding(12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Label badge (A / B / C / D)
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(labelBg.copy(alpha = if (state == OptionState.DEFAULT) 0.15f else 1f)),
            contentAlignment = Alignment.Center
        ) {
            when (state) {
                OptionState.CORRECT -> Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                OptionState.WRONG   -> Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(14.dp))
                else -> Text(
                    text  = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (state == OptionState.DEFAULT) MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )
            }
        }

        Text(
            text  = text,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────
// Phase 4 — Results
// ─────────────────────────────────────────────────────────────────────

@Composable
fun ResultsPhase(
    state: QuizUiState.Completed,
    onRetry: () -> Unit,
    onNewTopic: () -> Unit
) {
    val pct = state.score.toFloat() / state.total

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Score ring
        Box(contentAlignment = Alignment.Center) {
            val animatedProgress by animateFloatAsState(
                targetValue   = pct,
                animationSpec = tween(900, easing = ExpoOut),
                label         = "score-ring"
            )
            CircularProgressIndicator(
                progress     = animatedProgress,
                modifier     = Modifier.size(120.dp),
                color        = when {
                    pct >= 0.8f -> MaterialTheme.colorScheme.primary
                    pct >= 0.5f -> MaterialTheme.colorScheme.secondary
                    else        -> MaterialTheme.colorScheme.error
                },
                trackColor   = MaterialTheme.colorScheme.surfaceVariant,
                strokeWidth  = 8.dp,
                strokeCap    = StrokeCap.Round
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text  = "${state.score}/${state.total}",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text  = "Score",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = when {
                pct == 1f   -> "Perfect! All correct."
                pct >= 0.8f -> "Great work!"
                pct >= 0.6f -> "Good effort. Review the misses."
                pct >= 0.4f -> "Keep practising."
                else        -> "Revisit ${state.topic} and try again."
            },
            style     = MaterialTheme.typography.titleMedium,
            color     = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text  = state.topic,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(36.dp))

        PrimaryButton(text = "Retry Topic", enabled = true, onClick = onRetry)

        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable(onClick = onNewTopic)
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text  = "Try a Different Topic",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────
// Phase 5 — Error
// ─────────────────────────────────────────────────────────────────────

@Composable
fun ErrorPhase(message: String, onRetry: () -> Unit) {
    Column(
        modifier            = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text  = "Oops",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text      = message,
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(28.dp))
        PrimaryButton(text = "Try Again", enabled = true, onClick = onRetry)
    }
}

// ─────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────

private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

private operator fun <A, B, C, D> Tuple4<A, B, C, D>.component1() = a
private operator fun <A, B, C, D> Tuple4<A, B, C, D>.component2() = b
private operator fun <A, B, C, D> Tuple4<A, B, C, D>.component3() = c
private operator fun <A, B, C, D> Tuple4<A, B, C, D>.component4() = d
