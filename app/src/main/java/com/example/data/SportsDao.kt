package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SportsDao {
    @Query("SELECT * FROM saved_matches ORDER BY timestamp ASC")
    fun getSavedMatches(): Flow<List<SavedMatch>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: SavedMatch)

    @Query("DELETE FROM saved_matches WHERE matchId = :matchId")
    suspend fun deleteMatchById(matchId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM saved_matches WHERE matchId = :matchId)")
    fun isMatchSaved(matchId: String): Flow<Boolean>

    @Query("SELECT * FROM saved_channels")
    fun getSavedChannels(): Flow<List<SavedChannel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannel(channel: SavedChannel)

    @Query("DELETE FROM saved_channels WHERE channelId = :channelId")
    suspend fun deleteChannelById(channelId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM saved_channels WHERE channelId = :channelId)")
    fun isChannelSaved(channelId: String): Flow<Boolean>
}
