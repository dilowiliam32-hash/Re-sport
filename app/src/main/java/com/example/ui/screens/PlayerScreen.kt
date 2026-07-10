package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.VideoPlayer
import com.example.ui.theme.*
import com.example.viewmodel.SportsViewModel
import kotlinx.coroutines.launch

@Composable
fun PlayerScreen(
    viewModel: SportsViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedMatch by viewModel.selectedMatch.collectAsState()
    val selectedChannel by viewModel.selectedChannel.collectAsState()

    val selectedServer by viewModel.selectedServer.collectAsState()
    val selectedQuality by viewModel.selectedQuality.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val savedMatches by viewModel.savedMatches.collectAsState()
    val savedChannels by viewModel.savedChannels.collectAsState()

    var userMessageText by remember { mutableStateOf("") }
    val chatListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll chat to bottom when new messages arrive
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            chatListState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    val streamUrl = selectedMatch?.streamUrl ?: selectedChannel?.streamUrl ?: ""
    val title = if (selectedMatch != null) {
        "${selectedMatch!!.homeTeam} vs ${selectedMatch!!.awayTeam}"
    } else {
        selectedChannel?.name ?: "Sports Broadcast"
    }

    val subtitle = if (selectedMatch != null) {
        "${selectedMatch!!.leagueName} • ${selectedMatch!!.channelName}"
    } else {
        selectedChannel?.nowPlaying ?: "Live Stream"
    }

    val isSaved = if (selectedMatch != null) {
        savedMatches.any { it.matchId == selectedMatch!!.id }
    } else {
        selectedChannel?.let { ch -> savedChannels.any { it.channelId == ch.id } } ?: false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SportSlateBg)
    ) {
        // Player Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16 / 9f)
                .background(Color.Black)
        ) {
            if (streamUrl.isNotEmpty()) {
                VideoPlayer(
                    videoUrl = streamUrl,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Invalid Video Feed", color = SportTextSecondary)
                }
            }

            // Top overlay back button and favorite
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                IconButton(
                    onClick = {
                        selectedMatch?.let { viewModel.toggleMatchSaved(it) }
                        selectedChannel?.let { viewModel.toggleChannelSaved(it) }
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Save",
                        tint = if (isSaved) SportGreen else Color.White
                    )
                }
            }
        }

        // Info & Controls Tab Row
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(SportSlateBg),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title & Info
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = SportTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = subtitle,
                                fontSize = 12.sp,
                                color = SportTextSecondary
                            )
                        }

                        // Live viewer count badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SportLiveRed.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(SportLiveRed)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (selectedMatch != null) selectedMatch!!.viewersCount else (selectedChannel?.viewersCount ?: "0"),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SportLiveRed
                                )
                            }
                        }
                    }
                }
            }

            // Server / Broadcaster Source selection
            item {
                Column {
                    Text(
                        text = "SELECT STREAM SERVER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SportGreen,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val serversList = selectedMatch?.servers ?: listOf("Main HLS Stream", "Backup HD 1", "Backup SD 2")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        serversList.forEach { server ->
                            val isSelected = selectedServer == server
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) SportGreen else SportSurface)
                                    .clickable { viewModel.setServer(server) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = server,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.Black else SportTextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Quality switcher
            item {
                Column {
                    Text(
                        text = "STREAM QUALITY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SportTextSecondary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val qualities = listOf("1080p (Auto)", "720p HD", "480p SD")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        qualities.forEach { quality ->
                            val isSelected = selectedQuality == quality
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) SportSurfaceVariant else SportSurface)
                                    .clickable { viewModel.setQuality(quality) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = quality,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) SportGreen else SportTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Live Chat Panel Header
            item {
                Divider(color = SportSurfaceVariant, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ChatBubble,
                            contentDescription = "Chat",
                            tint = SportGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LIVE CHAT ROOM",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SportTextPrimary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(SportGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "ACTIVE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = SportGreen
                        )
                    }
                }
            }

            // Live Chat Scrollable Container
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SportSurface)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (chatMessages.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = SportGreen, modifier = Modifier.size(24.dp))
                            }
                        } else {
                            LazyColumn(
                                state = chatListState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(chatMessages) { chat ->
                                    if (chat.isSystem) {
                                        Text(
                                            text = chat.message,
                                            fontSize = 11.sp,
                                            color = SportTextSecondary,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(SportSurfaceVariant, RoundedCornerShape(4.dp))
                                                .padding(6.dp)
                                        )
                                    } else {
                                        val color = try {
                                            Color(android.graphics.Color.parseColor(chat.avatarColorHex))
                                        } catch (e: Exception) {
                                            SportGreen
                                        }

                                        Row(verticalAlignment = Alignment.Top) {
                                            // Avatar
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clip(CircleShape)
                                                    .background(color),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = chat.sender.firstOrNull()?.toString() ?: "",
                                                    color = Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))

                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = chat.sender,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (chat.sender == "You") SportGreen else SportTextPrimary
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "12:15",
                                                        fontSize = 8.sp,
                                                        color = SportTextSecondary
                                                    )
                                                }
                                                Text(
                                                    text = chat.message,
                                                    fontSize = 12.sp,
                                                    color = SportTextPrimary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Message send text field
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = userMessageText,
                        onValueChange = { userMessageText = it },
                        placeholder = { Text("Write a message...", fontSize = 13.sp, color = SportTextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SportGreen,
                            unfocusedBorderColor = SportSurfaceVariant,
                            focusedContainerColor = SportSurface,
                            unfocusedContainerColor = SportSurface,
                            focusedTextColor = SportTextPrimary,
                            unfocusedTextColor = SportTextPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("chat_input_field"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (userMessageText.isNotBlank()) {
                                viewModel.sendUserChatMessage(userMessageText)
                                userMessageText = ""
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(SportGreen)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send Message",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
