package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SportMatch
import com.example.ui.theme.*
import com.example.viewmodel.SportsViewModel

@Composable
fun HomeScreen(
    viewModel: SportsViewModel,
    onNavigateToPlayer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val matches by viewModel.filteredMatches.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedSport by viewModel.selectedSport.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    val savedMatches by viewModel.savedMatches.collectAsState()

    val sports = listOf("All", "Football", "Basketball", "Tennis", "MMA", "Rugby")
    val statuses = listOf("All", "LIVE", "UPCOMING", "FINISHED")

    // Find a premium featured live match for the hero banner
    val featuredMatch = matches.firstOrNull { it.status == "LIVE" } ?: matches.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SportSlateBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App header / Branding
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ZE SPORT",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = SportGreen,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Premium Live Streams & Scores",
                        fontSize = 12.sp,
                        color = SportTextSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SportSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsSoccer,
                        contentDescription = "Profile",
                        tint = SportGreen
                    )
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("Search matches, leagues, teams...", color = SportTextSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = SportGreen) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = SportTextSecondary)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SportGreen,
                    unfocusedBorderColor = SportSurfaceVariant,
                    focusedContainerColor = SportSurface,
                    unfocusedContainerColor = SportSurface,
                    focusedTextColor = SportTextPrimary,
                    unfocusedTextColor = SportTextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("match_search_input")
            )
        }

        // Sport Category Selector
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(sports) { sport ->
                    val isSelected = selectedSport == sport
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setSportFilter(sport) },
                        label = { Text(sport) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SportGreen,
                            selectedLabelColor = Color.Black,
                            containerColor = SportSurface,
                            labelColor = SportTextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = SportSurfaceVariant,
                            selectedBorderColor = SportGreen
                        )
                    )
                }
            }
        }

        // Hero Banner / Featured Live Match
        if (featuredMatch != null) {
            item {
                FeaturedMatchBanner(
                    match = featuredMatch,
                    onWatchClick = {
                        viewModel.selectMatch(featuredMatch)
                        onNavigateToPlayer()
                    }
                )
            }
        }

        // Status filter pills (LIVE, UPCOMING, FINISHED)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Match Schedule",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SportTextPrimary
                )
                
                // Status filtering options
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    statuses.forEach { status ->
                        val isSelected = selectedStatus == status
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) SportGreen else SportSurface)
                                .clickable { viewModel.setStatusFilter(status) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = status,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.Black else SportTextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Matches list
        if (matches.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SportsScore,
                            contentDescription = "No Matches",
                            modifier = Modifier.size(64.dp),
                            tint = SportTextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No matches found matching your filters.",
                            color = SportTextSecondary,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(matches) { match ->
                val isSaved = savedMatches.any { it.matchId == match.id }
                MatchCard(
                    match = match,
                    isSaved = isSaved,
                    onCardClick = {
                        viewModel.selectMatch(match)
                        onNavigateToPlayer()
                    },
                    onSaveClick = {
                        viewModel.toggleMatchSaved(match)
                    }
                )
            }
        }
    }
}

@Composable
fun FeaturedMatchBanner(
    match: SportMatch,
    onWatchClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SportSurface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            SportSurfaceVariant.copy(alpha = 0.6f),
                            SportSurface.copy(alpha = 0.95f)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            // Live badge top right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = match.leagueName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SportTextSecondary
                )

                if (match.status == "LIVE") {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(SportLiveRed)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "LIVE • ${match.minute}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(SportSurfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "UPCOMING",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SportLiveOrange
                        )
                    }
                }
            }

            // Teams & Score
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Team A
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(match.homeLogo, fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = match.homeTeam,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SportTextPrimary,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Score or VS
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 16.dp)) {
                        if (match.status == "LIVE" || match.status == "FINISHED") {
                            Text(
                                text = match.score,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = SportGreen,
                                letterSpacing = 1.sp
                            )
                        } else {
                            Text(
                                text = match.matchTime,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = SportTextPrimary
                            )
                            Text(
                                text = "VS",
                                fontSize = 10.sp,
                                color = SportTextSecondary
                            )
                        }
                    }

                    // Team B
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(match.awayLogo, fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = match.awayTeam,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SportTextPrimary,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Watch/Reminder bottom bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tv,
                        contentDescription = "Channel",
                        tint = SportGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = match.channelName,
                        fontSize = 11.sp,
                        color = SportTextSecondary
                    )
                }

                Button(
                    onClick = onWatchClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SportGreen,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        imageVector = if (match.status == "LIVE") Icons.Default.PlayArrow else Icons.Default.Notifications,
                        contentDescription = "Watch",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (match.status == "LIVE") "Watch Stream" else "Set Reminder",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun MatchCard(
    match: SportMatch,
    isSaved: Boolean,
    onCardClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SportSurface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // League and Status Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(match.leagueLogo, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = match.leagueName,
                        fontSize = 11.sp,
                        color = SportTextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (match.status == "LIVE") {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SportLiveRed.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "LIVE • ${match.minute}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SportLiveRed
                            )
                        }
                    } else if (match.status == "FINISHED") {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SportSurfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "FINISHED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SportTextSecondary
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SportLiveOrange.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = match.matchTime,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SportLiveOrange
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onSaveClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Save Match",
                            tint = if (isSaved) SportGreen else SportTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Teams Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home Team
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(match.homeLogo, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = match.homeTeam,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SportTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Scores or VS
                Box(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (match.status == "LIVE" || match.status == "FINISHED") {
                        Text(
                            text = match.score,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = if (match.status == "LIVE") SportGreen else SportTextPrimary,
                            letterSpacing = 0.5.sp
                        )
                    } else {
                        Text(
                            text = "vs",
                            fontSize = 13.sp,
                            color = SportTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Away Team
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = match.awayTeam,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SportTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(match.awayLogo, fontSize = 20.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Divider(color = SportSurfaceVariant, thickness = 1.dp)

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom channel / info bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tv,
                        contentDescription = "Broadcaster",
                        tint = SportGreen,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = match.channelName,
                        fontSize = 11.sp,
                        color = SportTextSecondary
                    )
                }

                if (match.status == "LIVE") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(SportGreen)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${match.viewersCount} watching",
                            fontSize = 11.sp,
                            color = SportGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else if (match.status == "UPCOMING") {
                    Text(
                        text = "Kicks off at ${match.matchTime}",
                        fontSize = 11.sp,
                        color = SportLiveOrange,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Text(
                        text = "Match Completed",
                        fontSize = 11.sp,
                        color = SportTextSecondary
                    )
                }
            }
        }
    }
}
