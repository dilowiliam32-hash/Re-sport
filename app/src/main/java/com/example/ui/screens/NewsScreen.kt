package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Article
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
import com.example.data.NewsArticle
import com.example.ui.theme.*
import com.example.viewmodel.SportsViewModel

@Composable
fun NewsScreen(
    viewModel: SportsViewModel,
    modifier: Modifier = Modifier
) {
    val newsList by viewModel.news.collectAsState()
    val selectedArticle by viewModel.selectedArticle.collectAsState()

    AnimatedContent(
        targetState = selectedArticle,
        transitionSpec = {
            if (targetState != null) {
                // slide in article detail
                slideInHorizontally { width -> width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> -width } + fadeOut()
            } else {
                // slide out back to news list
                slideInHorizontally { width -> -width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> width } + fadeOut()
            }
        },
        label = "NewsTransition"
    ) { article ->
        if (article != null) {
            ArticleDetail(
                article = article,
                onBackClick = { viewModel.selectArticle(null) }
            )
        } else {
            NewsListScreen(
                newsList = newsList,
                onArticleClick = { viewModel.selectArticle(it) },
                modifier = modifier
            )
        }
    }
}

@Composable
fun NewsListScreen(
    newsList: List<NewsArticle>,
    onArticleClick: (NewsArticle) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SportSlateBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // News header
        item {
            Column {
                Text(
                    text = "SPORTS CENTRAL",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = SportTextPrimary,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Latest announcements, insights, and highlight analysis",
                    fontSize = 12.sp,
                    color = SportTextSecondary
                )
            }
        }

        if (newsList.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SportGreen)
                }
            }
        } else {
            items(newsList) { article ->
                NewsArticleCard(
                    article = article,
                    onClick = { onArticleClick(article) }
                )
            }
        }
    }
}

@Composable
fun NewsArticleCard(
    article: NewsArticle,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SportSurface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Category & Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(SportGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = article.category.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SportGreen
                    )
                }

                Text(
                    text = article.timeAgo,
                    fontSize = 11.sp,
                    color = SportTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Body info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Large styled emoji/letter representing article thumbnail since we are offline
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SportSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Article,
                        contentDescription = "News icon",
                        tint = SportGreen,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = article.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SportTextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = article.summary,
                        fontSize = 12.sp,
                        color = SportTextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ArticleDetail(
    article: NewsArticle,
    onBackClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SportSlateBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Navigation bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = SportTextPrimary
                    )
                }

                IconButton(onClick = { /* Share stub */ }) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = SportGreen
                    )
                }
            }
        }

        // Title and meta
        item {
            Column {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(SportGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = article.category.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SportGreen
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = article.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = SportTextPrimary,
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Published • ${article.timeAgo}",
                    fontSize = 12.sp,
                    color = SportTextSecondary
                )
            }
        }

        // Large Decorative banner placeholder
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SportSurface),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Article,
                        contentDescription = "News Decor",
                        modifier = Modifier.size(64.dp),
                        tint = SportGreen.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "ZE SPORT NEWS HUB",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SportGreen.copy(alpha = 0.6f),
                        letterSpacing = 1.5.sp
                    )
                }
            }
        }

        // Summary Highlight Box
        item {
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = SportSurfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = article.summary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SportTextPrimary,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // Full Body Text
        item {
            Text(
                text = article.body,
                fontSize = 14.sp,
                color = SportTextPrimary,
                lineHeight = 22.sp,
                textAlign = TextAlign.Justify
            )
        }
    }
}
