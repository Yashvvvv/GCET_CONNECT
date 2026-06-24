package app.recruit.collegebot.presentation.ui.chat

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.recruit.collegebot.domain.model.MessageModel
import app.recruit.collegebot.presentation.viewmodel.ChatUiState
import app.recruit.collegebot.presentation.viewmodel.ChatViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.collegebot.R
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import androidx.compose.ui.draw.alpha

private val ExpoOut = Easing { t ->
    if (t == 1f) 1f else 1f - Math.pow(2.0, -10.0 * t).toFloat()
}

// ─────────────────────────────────────────────────────────────────────
// ChatPage
// ─────────────────────────────────────────────────────────────────────

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatPage(
    modifier: Modifier = Modifier,
    onStudyClick: () -> Unit = {},
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var userInput by remember { mutableStateOf(TextFieldValue("")) }
    val listState = rememberLazyListState()
    var showSuggestions by remember { mutableStateOf(false) }

    BackHandler(enabled = !uiState.showWelcome) {
        viewModel.clearChat()
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        modifier       = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ChatTopBar(
                isStreaming  = uiState.isStreaming,
                showClear    = !uiState.showWelcome,
                onClearChat  = { viewModel.clearChat() },
                onStudyClick = onStudyClick
            )
        },
        bottomBar = {
            if (!uiState.showWelcome) {
                Column {
                    // Error banner
                    AnimatedVisibility(
                        visible = uiState.error != null,
                        enter   = expandVertically(tween(200)) + fadeIn(tween(200)),
                        exit    = shrinkVertically(tween(150)) + fadeOut(tween(150))
                    ) {
                        ErrorBanner(
                            message   = uiState.error ?: "",
                            onRetry   = { viewModel.retryLast() },
                            onDismiss = { viewModel.dismissError() }
                        )
                    }

                    // Suggestions tray
                    AnimatedVisibility(
                        visible = showSuggestions,
                        enter   = expandVertically(tween(220, easing = ExpoOut)) + fadeIn(tween(180)),
                        exit    = shrinkVertically(tween(160)) + fadeOut(tween(120))
                    ) {
                        LazyRow(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(commonQueries) { q ->
                                QuickChip(text = q, onClick = {
                                    viewModel.sendMessage(q)
                                    showSuggestions = false
                                })
                            }
                        }
                    }

                    ChatInputBar(
                        value               = userInput,
                        onChange            = { userInput = it },
                        onSend              = {
                            if (userInput.text.isNotBlank() && !uiState.isStreaming) {
                                viewModel.sendMessage(userInput.text)
                                userInput = TextFieldValue("")
                            }
                        },
                        onToggleSuggestions = { showSuggestions = !showSuggestions },
                        suggestionsOpen     = showSuggestions,
                        enabled             = !uiState.isStreaming
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.showWelcome) {
                WelcomeContent(
                    modifier = Modifier.fillMaxSize(),
                    onSend   = { query ->
                        viewModel.sendMessage(query)
                    }
                )
            } else {
                LazyColumn(
                    modifier            = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    state               = listState,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { Spacer(Modifier.height(8.dp)) }
                    itemsIndexed(uiState.messages) { index, message ->
                        val isLastMessage = index == uiState.messages.lastIndex
                        MessageRow(
                            message     = message,
                            showCursor  = isLastMessage && uiState.isStreaming && message.role == "model"
                        )
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────
// Top bar
// ─────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(
    isStreaming: Boolean,
    showClear: Boolean,
    onClearChat: () -> Unit,
    onStudyClick: () -> Unit = {}
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Image(
                    painter            = painterResource(R.drawable.gcet_logo),
                    contentDescription = null,
                    modifier           = Modifier.size(34.dp).clip(CircleShape),
                    contentScale       = ContentScale.Crop
                )
                Column {
                    Text(
                        text  = "GCET Connect",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Live status dot
                        val dotAlpha by rememberInfiniteTransition(label = "dot").animateFloat(
                            initialValue  = 1f,
                            targetValue   = 0.3f,
                            animationSpec = infiniteRepeatable(
                                animation  = tween(900, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "dot-alpha"
                        )
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isStreaming) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.primary
                                )
                                .alpha(if (isStreaming) dotAlpha else 1f)
                        )
                        Text(
                            text  = if (isStreaming) "Typing…" else "Online",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        actions = {
            IconButton(onClick = onStudyClick) {
                Icon(
                    imageVector        = Icons.Default.MenuBook,
                    contentDescription = "Study mode",
                    tint               = MaterialTheme.colorScheme.primary
                )
            }
            if (showClear) {
                IconButton(onClick = onClearChat) {
                    Icon(
                        imageVector        = Icons.Default.Delete,
                        contentDescription = "Clear chat",
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

// ─────────────────────────────────────────────────────────────────────
// Error banner
// ─────────────────────────────────────────────────────────────────────

@Composable
fun ErrorBanner(
    message: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        color  = MaterialTheme.colorScheme.errorContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text     = message,
                style    = MaterialTheme.typography.bodySmall,
                color    = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            Row {
                IconButton(onClick = onRetry, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Retry",
                        tint               = MaterialTheme.colorScheme.onErrorContainer,
                        modifier           = Modifier.size(16.dp)
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint               = MaterialTheme.colorScheme.onErrorContainer,
                        modifier           = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────
// Message bubble
// ─────────────────────────────────────────────────────────────────────

@Composable
fun MessageRow(message: MessageModel, showCursor: Boolean = false) {
    val isBot = message.role == "model"

    AnimatedVisibility(
        visible = true,
        enter   = fadeIn(tween(220, easing = ExpoOut)) +
                  slideInVertically(tween(220, easing = ExpoOut)) { it / 5 }
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isBot) Arrangement.Start else Arrangement.End,
            verticalAlignment     = Alignment.Bottom
        ) {
            if (isBot) {
                AvatarCircle(resId = R.drawable.bot_avatar, tinted = true)
                Spacer(Modifier.width(8.dp))
            }

            val shape = if (isBot)
                RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 18.dp)
            else
                RoundedCornerShape(topStart = 18.dp, topEnd = 4.dp, bottomEnd = 18.dp, bottomStart = 18.dp)

            Surface(
                shape    = shape,
                color    = if (isBot) MaterialTheme.colorScheme.surfaceVariant
                           else MaterialTheme.colorScheme.primary,
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Box(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    when {
                        message.isTyping -> TypingIndicator()
                        showCursor       -> StreamingText(message.message)
                        else             -> Text(
                            text  = message.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isBot) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            if (!isBot) {
                Spacer(Modifier.width(8.dp))
                AvatarCircle(resId = R.drawable.user_avatar, tinted = false)
            }
        }
    }
}

// Streaming text with blinking cursor
@Composable
fun StreamingText(text: String) {
    val transition = rememberInfiniteTransition(label = "cursor")
    val cursorAlpha by transition.animateFloat(
        initialValue  = 1f,
        targetValue   = 0f,
        animationSpec = infiniteRepeatable(
            animation  = tween(530, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor-alpha"
    )

    Text(
        text = buildAnnotatedString {
            append(text)
            withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary.copy(alpha = cursorAlpha))) {
                append("▌")
            }
        },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun AvatarCircle(resId: Int, tinted: Boolean) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(
                if (tinted) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter            = painterResource(resId),
            contentDescription = null,
            modifier           = Modifier.size(30.dp).clip(CircleShape),
            contentScale       = ContentScale.Crop
        )
    }
}

// ─────────────────────────────────────────────────────────────────────
// Typing indicator — bouncing dots
// ─────────────────────────────────────────────────────────────────────

@Composable
fun TypingIndicator() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment     = Alignment.CenterVertically,
        modifier              = Modifier.padding(vertical = 4.dp)
    ) {
        repeat(3) { index ->
            val transition = rememberInfiniteTransition(label = "dot$index")
            val offsetY by transition.animateFloat(
                initialValue  = 0f,
                targetValue   = -5f,
                animationSpec = infiniteRepeatable(
                    animation  = tween(380, delayMillis = index * 110, easing = ExpoOut),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot-y-$index"
            )
            Box(
                modifier = Modifier
                    .offset { IntOffset(0, offsetY.roundToInt()) }
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────
// Input bar
// ─────────────────────────────────────────────────────────────────────

@Composable
fun ChatInputBar(
    value: TextFieldValue,
    onChange: (TextFieldValue) -> Unit,
    onSend: () -> Unit,
    onToggleSuggestions: () -> Unit,
    suggestionsOpen: Boolean,
    enabled: Boolean
) {
    Surface(
        color          = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick  = onToggleSuggestions,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector        = if (suggestionsOpen) Icons.Default.KeyboardArrowDown
                                        else Icons.Default.KeyboardArrowUp,
                    contentDescription = "Suggestions",
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            CleanTextField(
                value    = value,
                onChange = onChange,
                onSend   = onSend,
                enabled  = enabled,
                modifier = Modifier.weight(1f)
            )

            SendButton(
                onClick  = onSend,
                enabled  = value.text.isNotBlank() && enabled
            )
        }
    }
}

@Composable
fun CleanTextField(
    value: TextFieldValue,
    onChange: (TextFieldValue) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    var focused by remember { mutableStateOf(false) }

    val borderAlpha by animateFloatAsState(
        targetValue   = if (focused && enabled) 1f else 0f,
        animationSpec = tween(180, easing = ExpoOut),
        label         = "border-alpha"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(
                    alpha = if (enabled) 1f else 0.6f
                )
            )
            .border(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = borderAlpha),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        BasicTextField(
            value         = value,
            onValueChange = if (enabled) onChange else { _ -> },
            modifier      = Modifier
                .fillMaxWidth()
                .onFocusChanged { focused = it.isFocused },
            enabled       = enabled,
            textStyle     = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush   = SolidColor(MaterialTheme.colorScheme.primary),
            maxLines      = 4,
            decorationBox = { inner ->
                if (value.text.isEmpty()) {
                    Text(
                        text  = if (enabled) "Ask anything about GCET…"
                                else "Waiting for response…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                    )
                }
                inner()
            }
        )
    }
}

@Composable
fun SendButton(onClick: () -> Unit, enabled: Boolean) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val buttonScale by animateFloatAsState(
        targetValue   = when {
            isPressed -> 0.88f
            enabled   -> 1f
            else      -> 0.82f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessHigh
        ),
        label = "send-scale"
    )

    Box(
        modifier = Modifier
            .scale(buttonScale)
            .size(40.dp)
            .clip(CircleShape)
            .background(
                if (enabled) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(
                interactionSource = interactionSource,
                indication        = null,
                enabled           = enabled,
                onClick           = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector        = Icons.AutoMirrored.Filled.Send,
            contentDescription = "Send",
            tint               = if (enabled) MaterialTheme.colorScheme.onPrimary
                                 else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier           = Modifier.size(18.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────
// Quick chip
// ─────────────────────────────────────────────────────────────────────

@Composable
fun QuickChip(text: String, onClick: () -> Unit) {
    SuggestionChip(
        onClick = onClick,
        label   = { Text(text, style = MaterialTheme.typography.labelMedium) },
        shape   = RoundedCornerShape(20.dp),
        colors  = SuggestionChipDefaults.suggestionChipColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
            labelColor     = MaterialTheme.colorScheme.onSurface
        ),
        border  = SuggestionChipDefaults.suggestionChipBorder(
            borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
            borderWidth = 1.dp
        )
    )
}

// ─────────────────────────────────────────────────────────────────────
// Welcome screen
// ─────────────────────────────────────────────────────────────────────

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WelcomeContent(
    modifier: Modifier = Modifier,
    onSend: (String) -> Unit
) {
    var userInput    by remember { mutableStateOf(TextFieldValue("")) }
    var chipsVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(250)
        chipsVisible = true
    }

    Column(
        modifier            = modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val composition by rememberLottieComposition(
            LottieCompositionSpec.RawRes(R.raw.chat_animation)
        )
        val progress by animateLottieCompositionAsState(
            composition = composition,
            iterations  = LottieConstants.IterateForever
        )

        Box(
            modifier         = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            LottieAnimation(
                composition = composition,
                progress    = { progress },
                modifier    = Modifier.size(140.dp)
            )
        }

        Spacer(Modifier.height(28.dp))

        Text(
            text      = "GCET Connect",
            style     = MaterialTheme.typography.headlineMedium,
            color     = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text      = "Ask about timetables, exams, hostel,\nplacement, departments, and more.",
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(28.dp))

        Surface(
            shape          = RoundedCornerShape(26.dp),
            color          = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier       = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier              = Modifier.padding(8.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CleanTextField(
                    value    = userInput,
                    onChange = { userInput = it },
                    onSend   = { if (userInput.text.isNotBlank()) onSend(userInput.text) },
                    modifier = Modifier.weight(1f)
                )
                SendButton(
                    onClick = { if (userInput.text.isNotBlank()) onSend(userInput.text) },
                    enabled = userInput.text.isNotBlank()
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement   = Arrangement.spacedBy(8.dp),
            modifier              = Modifier.fillMaxWidth()
        ) {
            quickActions.forEachIndexed { index, (label, query) ->
                var itemVisible by remember { mutableStateOf(false) }
                LaunchedEffect(chipsVisible) {
                    if (chipsVisible) {
                        delay(index * 65L)
                        itemVisible = true
                    }
                }
                val chipAlpha by animateFloatAsState(
                    targetValue   = if (itemVisible) 1f else 0f,
                    animationSpec = tween(200, easing = ExpoOut),
                    label         = "chip-alpha-$index"
                )
                val chipScale by animateFloatAsState(
                    targetValue   = if (itemVisible) 1f else 0.90f,
                    animationSpec = tween(200, easing = ExpoOut),
                    label         = "chip-scale-$index"
                )
                Box(
                    modifier = Modifier
                        .scale(chipScale)
                        .alpha(chipAlpha)
                ) {
                    QuickChip(text = label, onClick = { onSend(query) })
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────
// Static data
// ─────────────────────────────────────────────────────────────────────

val commonQueries = listOf(
    "Class timings", "Exam schedule", "Library hours",
    "Placement info", "Bus routes", "Hostel facilities"
)

private val quickActions = listOf(
    "Academic calendar" to "What is the academic calendar for GCET?",
    "Departments"       to "List all departments in GCET",
    "Placement stats"   to "What are GCET placement statistics?",
    "Contact directory" to "Important contact numbers for GCET"
)
