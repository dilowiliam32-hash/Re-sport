package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SavedChannel
import com.example.data.SavedMatch
import com.example.data.SportMatch
import com.example.data.LiveChannel
import com.example.ui.theme.*
import com.example.viewmodel.SportsViewModel

@Composable
fun WatchlistScreen(
    viewModel: SportsViewModel,
    onNavigateToPlayer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val savedMatches by viewModel.savedMatches.collectAsState()
    val savedChannels by viewModel.savedChannels.collectAsState()
    val allMatches by viewModel.allMatches.collectAsState()
    val allChannels by viewModel.channels.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0 = Matches, 1 = TV Channels

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SportSlateBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "MY FAVORITES",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = SportTextPrimary,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Manage your scheduled match reminders and saved TV channels",
                    fontSize = 12.sp,
                    color = SportTextSecondary
                )
            }
        }

        // Segmented Tabs
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SportSurface,
                contentColor = SportGreen,
                modifier = Modifier.clip(RoundedCornerShape(10.dp)),
                divider = {}
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Matches (${savedMatches.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Channels (${savedChannels.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
            }
        }

        // Empty states or saved entries
        if (selectedTab == 0) {
            if (savedMatches.isEmpty()) {
                item {
                    WatchlistEmptyState(
                        message = "You have no saved matches. Tap the star icon on any match in the schedule to save it or set alerts!",
                        icon = Icons.Outlined.StarBorder
                    )
                }
            } else {
                items(savedMatches) { saved ->
                    // Find matching SportMatch in all matches to get stream url and updated status
                    val originalMatch = allMatches.firstOrNull { it.id == saved.matchId } ?: SportMatch(
                        id = saved.matchId,
                        homeTeam = saved.homeTeam,
                        awayTeam = saved.awayTeam,
                        homeLogo = saved.homeLogo,
                        awayLogo = saved.awayLogo,
                        leagueName = saved.leagueName,
                        leagueLogo = "⚽",
                        status = "UPCOMING",
                        matchTime = saved.matchTime,
                        streamUrl = "",
                        sportType = "Football",
                        channelName = "TF1"
                    )

                    SavedMatchCard(
                        match = saved,
                        onClick = {
                            viewModel.selectMatch(originalMatch)
                            onNavigateToPlayer()
                        },
                        onDeleteClick = {
                            viewModel.toggleMatchSaved(originalMatch)
                        }
                    )
                }
            }
        } else {
            if (savedChannels.isEmpty()) {
                item {
                    WatchlistEmptyState(
                        message = "You have no favorite channels. Save TV channels from the Channels tab for immediate access!",
                        icon = Icons.Outlined.StarBorder
                    )
                }
            } else {
                items(savedChannels) { saved ->
                    // Find original live channel to preserve custom logo values
                    val originalChannel = allChannels.firstOrNull { it.id == saved.channelId } ?: LiveChannel(
                        id = saved.channelId,
                        name = saved.name,
                        logoText = "TV",
                        logoColorHex = saved.logoUrl,
                        nowPlaying = "Live Broadcast",
                        viewersCount = "10k",
                        category = "Other",
                        streamUrl = saved.streamUrl
                    )

                    SavedChannelCard(
                        channel = saved,
                        logoColorHex = originalChannel.logoColorHex,
                        logoText = originalChannel.logoText,
                        onClick = {
                            viewModel.selectChannel(originalChannel)
                            onNavigateToPlayer()
                        },
                        onDeleteClick = {
                            viewModel.toggleChannelSaved(originalChannel)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun WatchlistEmptyState(message: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(SportSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = "Empty",
                    modifier = Modifier.size(32.dp),
                    tint = SportGreen
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Watchlist is Empty",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = SportTextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                fontSize = 12.sp,
                color = SportTextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun SavedMatchCard(
    match: SavedMatch,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SportSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Alert active",
                        tint = SportGreen,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = match.leagueName,
                        fontSize = 10.sp,
                        color = SportGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "${match.homeLogo} ${match.homeTeam} vs ${match.awayTeam} ${match.awayLogo}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SportTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Scheduled Time: ${match.matchTime}",
                    fontSize = 11.sp,
                    color = SportTextSecondary
                )
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete from Watchlist",
                    tint = SportLiveRed,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun SavedChannelCard(
    channel: SavedChannel,
    logoColorHex: String,
    logoText: String,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val logoColor = try {
        Color(android.graphics.Color.parseColor(logoColorHex))
    } catch (e: Exception) {
        SportGreenDark
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SportSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(logoColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = logoText,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = channel.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SportTextPrimary
                    )
                    Text(
                        text = "Favorite TV Channel",
                        fontSize = 11.sp,
                        color = SportTextSecondary
                    )
                }
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Favorite Channel",
                    tint = SportLiveRed,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
