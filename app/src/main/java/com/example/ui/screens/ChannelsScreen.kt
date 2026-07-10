package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.outlined.FavoriteBorder
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
import com.example.data.LiveChannel
import com.example.ui.theme.*
import com.example.viewmodel.SportsViewModel

@Composable
fun ChannelsScreen(
    viewModel: SportsViewModel,
    onNavigateToPlayer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val channels by viewModel.channels.collectAsState()
    val savedChannels by viewModel.savedChannels.collectAsState()

    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Premium", "Football", "Other")

    val filteredChannels = if (selectedCategory == "All") {
        channels
    } else {
        channels.filter { it.category == selectedCategory }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SportSlateBg)
            .padding(16.dp)
    ) {
        // Title
        Text(
            text = "LIVE TV CHANNELS",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = SportTextPrimary,
            letterSpacing = 0.5.sp
        )
        Text(
            text = "Stream real-time global sports networks",
            fontSize = 12.sp,
            color = SportTextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Category Tabs
        ScrollableTabRow(
            selectedTabIndex = categories.indexOf(selectedCategory),
            containerColor = Color.Transparent,
            contentColor = SportGreen,
            edgePadding = 0.dp,
            divider = {}
        ) {
            categories.forEachIndexed { index, cat ->
                val isSelected = selectedCategory == cat
                Tab(
                    selected = isSelected,
                    onClick = { selectedCategory = cat },
                    text = {
                        Text(
                            text = cat,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    selectedContentColor = SportGreen,
                    unselectedContentColor = SportTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grid of Channels
        if (filteredChannels.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No channels found in this category.",
                    color = SportTextSecondary,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredChannels) { channel ->
                    val isFavorite = savedChannels.any { it.channelId == channel.id }
                    ChannelCard(
                        channel = channel,
                        isFavorite = isFavorite,
                        onClick = {
                            viewModel.selectChannel(channel)
                            onNavigateToPlayer()
                        },
                        onFavoriteClick = {
                            viewModel.toggleChannelSaved(channel)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ChannelCard(
    channel: LiveChannel,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    val logoColor = try {
        Color(android.graphics.Color.parseColor(channel.logoColorHex))
    } catch (e: Exception) {
        SportGreenDark
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SportSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Logo emblem & Favorite button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular color badge representing broadcaster
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(logoColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = channel.logoText,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                // Favorite
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite Channel",
                        tint = if (isFavorite) SportLiveRed else SportTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Channel name and info
            Column {
                Text(
                    text = channel.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SportTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = channel.nowPlaying,
                    fontSize = 11.sp,
                    color = SportTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Viewers count and stream tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(SportGreen)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${channel.viewersCount} live",
                        fontSize = 10.sp,
                        color = SportGreen,
                        fontWeight = FontWeight.Medium
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(SportSurfaceVariant)
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "FHD stream",
                        fontSize = 9.sp,
                        color = SportTextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
