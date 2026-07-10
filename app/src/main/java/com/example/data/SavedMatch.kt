package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_matches")
data class SavedMatch(
    @PrimaryKey val matchId: String,
    val homeTeam: String,
    val awayTeam: String,
    val homeLogo: String,
    val awayLogo: String,
    val leagueName: String,
    val matchTime: String,
    val timestamp: Long,
    val isReminderSet: Boolean = false
)
