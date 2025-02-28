package com.example.collegebot

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatPage(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = viewModel()
) {
    val messageList = viewModel.messageList
    var userInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var showSuggestions by remember { mutableStateOf(false) }
    var showSplashAnimation by remember { mutableStateOf(true) }
    
    // Handle back press
    BackHandler(enabled = viewModel.showMessages.value) {
        // When back is pressed, hide messages and show welcome screen
        viewModel.toggleMessageVisibility(false)
    }
    
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.gcet_logo),
                            contentDescription = "GCET Logo",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "GCET Connect",
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Your College Assistant",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        bottomBar = {
            // Only show bottom bar when messages are visible
            if (viewModel.showMessages.value) {
                Column {
                    // Suggestions chips
                    AnimatedVisibility(
                        visible = showSuggestions,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        LazyRow(
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(commonQueries) { suggestion ->
                                SuggestionChip(
                                    onClick = {
                                        viewModel.sendMessage(suggestion)
                                        showSuggestions = false
                                    },
                                    label = { Text(suggestion) },
                                    shape = RoundedCornerShape(16.dp),
                                    border = SuggestionChipDefaults.suggestionChipBorder(

                                        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),

                                    )
                                )
                            }
                        }
                    }

                    // Input field and send button
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        tonalElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { showSuggestions = !showSuggestions }) {
                                Icon(
                                    if (showSuggestions) Icons.Default.KeyboardArrowDown 
                                    else Icons.Default.KeyboardArrowUp,
                                    "Toggle Suggestions"
                                )
                            }
                            
                            // Always show the text field
                            ShinyBorderTextField(
                                value = userInput,
                                onValueChange = { userInput = it },
                                placeholder = { 
                                    Text(
                                        "Ask me anything about GCET...",
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    ) 
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 8.dp),
                                showSplashAnimation = showSplashAnimation // Pass the flag
                            )
                            
                            AnimatedSendButton(
                                onClick = {
                                    if (userInput.isNotBlank()) {
                                        viewModel.sendMessage(userInput)
                                        userInput = ""
                                        coroutineScope.launch {
                                            listState.animateScrollToItem(messageList.size)
                                        }
                                    }
                                },
                                enabled = userInput.isNotBlank()
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
            .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (!viewModel.showMessages.value || messageList.isEmpty()) {
                // Welcome screen with Lottie animation
                WelcomeContent(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    onSuggestionClick = { query ->
                        viewModel.sendMessage(query)
                        viewModel.toggleMessageVisibility(true)
                    }
                )
            } else {
                // Chat messages
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messageList) { message ->
                        MessageRow(message)
                    }
                }
            }
        }
    }

    // Handle splash animation logic
    if (showSplashAnimation) {
        LaunchedEffect(Unit) {
            // Simulate splash screen duration
            delay(3000) // Adjust duration as needed
            showSplashAnimation = false
        }
    }
}

@Composable
fun MessageRow(messageModel: MessageModel) {
    val isBot = messageModel.role == "model"
    
    // Animation for message appearance
    val enterTransition = remember { fadeIn(animationSpec = tween(300)) }
    
    AnimatedVisibility(visible = true, enter = enterTransition) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isBot) Arrangement.Start else Arrangement.End
        ) {
            if (isBot) {
                Image(
                    painter = painterResource(id = R.drawable.bot_avatar),
                    contentDescription = "Bot Avatar",
        modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .align(Alignment.Top)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isBot) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.widthIn(max = 300.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (messageModel.isTyping) {
                        TypingIndicator()  // Show typing indicator
                    } else {
        Text(
                            text = messageModel.message,
                            color = if (isBot) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            if (!isBot) {
                Spacer(modifier = Modifier.width(8.dp))
                Image(
                    painter = painterResource(id = R.drawable.user_avatar),
                    contentDescription = "User Avatar",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .align(Alignment.Top)
                )
            }
        }
    }
}

@Composable
fun LoadingAnimation() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(8.dp)
    ) {
        repeat(3) { index ->
            val infiniteTransition = rememberInfiniteTransition()
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(index * 200)
                )
            )
            
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha))
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WelcomeContent(
    modifier: Modifier = Modifier,
    onSuggestionClick: (String) -> Unit
) {
    var userInput by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    // Animation states
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                    )
                )
            )
    ) {
        // Add subtle floating circles in background
        FloatingCircles()
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .scale(scale),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Lottie Animation with enhanced size and shadow
            val composition by rememberLottieComposition(
                LottieCompositionSpec.RawRes(R.raw.chat_animation)
            )
            val progress by animateLottieCompositionAsState(
                composition = composition,
                iterations = LottieConstants.IterateForever
            )
            
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                            )
                        ),
                        shape = CircleShape
                    )
            ) {
                LottieAnimation(
                    composition = composition,
                    progress = progress,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Animated welcome text
            AnimatedText(
                text = "Welcome to GCET Connect!",
                style = MaterialTheme.typography.headlineMedium.copy(
                    shadow = Shadow(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        offset = Offset(2f, 2f),
                        blurRadius = 4f
                    )
                ),
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Your AI-Powered College Assistant",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(0.8f)
            )

            // Enhanced input field
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(24.dp),
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                    ),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    ShinyBorderTextField(
                        value = userInput,
                        onValueChange = { userInput = it },
                        modifier = Modifier
                            .weight(1f),
                        placeholder = {
                            Text(
                                "Ask me anything about GCET...",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        },
                        showSplashAnimation = true
                    )
                    
                    AnimatedSendButton(
                        onClick = {
                            if (userInput.isNotBlank()) {
                                onSuggestionClick(userInput)
                                userInput = ""
                            }
                        },
                        enabled = userInput.isNotBlank()
                    )
                }
            }

            // Animated quick action chips
            AnimatedChipsRow(
                suggestions = listOf(
                    Pair("Campus Tour", "Tell me about campus tour"),
                    Pair("Academic Calendar", "What is the academic calendar"),
                    Pair("Department Info", "List of departments in GCET"),
                    Pair("Contact Directory", "Important contact numbers")
                ),
                onSuggestionClick = onSuggestionClick
            )
        }
    }
}

@Composable
private fun AnimatedText(
    text: String,
    style: TextStyle,
    fontWeight: FontWeight
) {
    var textVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        textVisible = true
    }

    val springAnimation = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    Text(
        text = text,
        style = style,
        fontWeight = fontWeight,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .scale(
                animateFloatAsState(
                    targetValue = if (textVisible) 1f else 0f,
                    animationSpec = springAnimation
                ).value
            )
            .alpha(
                animateFloatAsState(
                    targetValue = if (textVisible) 1f else 0f,
                    animationSpec = tween(1000)
                ).value
            )
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AnimatedChipsRow(
    suggestions: List<Pair<String, String>>,
    onSuggestionClick: (String) -> Unit
) {
    FlowRow(
        modifier = Modifier.padding(top = 8.dp),
        horizontalArrangement = Arrangement.Center,
        maxItemsInEachRow = 2
    ) {
        suggestions.forEachIndexed { index, (text, query) ->
            var visible by remember { mutableStateOf(false) }
            
            LaunchedEffect(Unit) {
                delay(index * 100L)
                visible = true
            }

            QuickActionChip(
                text = text,
                onClick = { onSuggestionClick(query) },
                modifier = Modifier
                    .scale(
                        animateFloatAsState(
                            targetValue = if (visible) 1f else 0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy
                            )
                        ).value
                    )
            )
        }
    }
}

@Composable
fun QuickActionChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SuggestionChip(
        onClick = onClick,
        label = { Text(text) },
        modifier = modifier.padding(4.dp),
        shape = RoundedCornerShape(16.dp),
        border = SuggestionChipDefaults.suggestionChipBorder(
            borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
    )
}

@Composable
private fun AnimatedSendButton(
    onClick: () -> Unit,
    enabled: Boolean
) {
    val scale by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy
        )
    )

    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.scale(scale)
    ) {
        Icon(
            Icons.Default.Send,
            contentDescription = "Send",
            tint = if (enabled) 
                MaterialTheme.colorScheme.primary 
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Common queries for suggestions
val commonQueries = listOf(
    "Class timings",
    "Exam schedule",
    "Library location",
    "Placement info",
    "Bus service",
    "Hostel facilities"
)

@Composable
fun ShinyBorderTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: @Composable () -> Unit,
    showSplashAnimation: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition()
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val shimmerColors = listOf(
        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = shimmerColors,
                    start = Offset(offset, offset),
                    end = Offset(offset + 100f, offset + 100f)
                ),
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            placeholder = placeholder,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            maxLines = 3
        )
    }
}

// Add this new composable for floating circles effect
@Composable
private fun FloatingCircles() {
    Box(modifier = Modifier.fillMaxSize()) {
        repeat(6) { index ->
            val infiniteTransition = rememberInfiniteTransition()
            
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 4000,
                        delayMillis = index * 1000,
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                )
            )

            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.7f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 4000,
                        delayMillis = index * 1000,
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                )
            )

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .offset(
                        x = (index * 70).dp,
                        y = (index * 60).dp
                    )
                    .scale(scale)
                    .alpha(alpha)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.0f)
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(modifier = Modifier.padding(8.dp)) {
        repeat(3) { index ->
            val infiniteTransition = rememberInfiniteTransition()
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = index * 200),
                    repeatMode = RepeatMode.Reverse
                )
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha))
            )
        }
    }
}

@Composable
fun ChatBackground() {
    val transition = rememberInfiniteTransition()
    val color by transition.animateColor(
        initialValue = Color(0xFF1E3D59),
        targetValue = Color(0xFF2D5F8B),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
    )
}
