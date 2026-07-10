package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_channels")
data class SavedChannel(
    @PrimaryKey val channelId: String,
    val name: String,
    val logoUrl: String,
    val streamUrl: String
)
