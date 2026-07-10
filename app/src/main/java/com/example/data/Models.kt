package com.example.data

data class SportMatch(
    val id: String,
    val homeTeam: String,
    val awayTeam: String,
    val homeLogo: String, // emoji or image code
    val awayLogo: String,
    val leagueName: String,
    val leagueLogo: String,
    val status: String, // "LIVE", "UPCOMING", "FINISHED"
    val matchTime: String,
    val score: String = "0 - 0",
    val minute: String = "",
    val streamUrl: String,
    val sportType: String, // "Football", "Basketball", "Tennis", "Rugby", "MMA"
    val channelName: String,
    val viewersCount: String = "0",
    val servers: List<String> = listOf("Server Premium FHD", "Server Backup HD", "Server Mobile SD")
)

data class LiveChannel(
    val id: String,
    val name: String,
    val logoText: String, // Initials or short name
    val logoColorHex: String,
    val nowPlaying: String,
    val viewersCount: String,
    val category: String, // "Premium", "Football", "Other"
    val streamUrl: String
)

data class NewsArticle(
    val id: String,
    val title: String,
    val category: String,
    val timeAgo: String,
    val imageUrl: String,
    val summary: String,
    val body: String
)

data class ChatMessage(
    val id: String,
    val sender: String,
    val avatarColorHex: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSystem: Boolean = false
)
