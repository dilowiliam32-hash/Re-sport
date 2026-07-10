package com.example.data

import kotlinx.coroutines.flow.Flow

class SportsRepository(private val sportsDao: SportsDao) {
    val savedMatches: Flow<List<SavedMatch>> = sportsDao.getSavedMatches()
    val savedChannels: Flow<List<SavedChannel>> = sportsDao.getSavedChannels()

    suspend fun saveMatch(match: SavedMatch) {
        sportsDao.insertMatch(match)
    }

    suspend fun removeMatch(matchId: String) {
        sportsDao.deleteMatchById(matchId)
    }

    fun isMatchSaved(matchId: String): Flow<Boolean> {
        return sportsDao.isMatchSaved(matchId)
    }

    suspend fun saveChannel(channel: SavedChannel) {
        sportsDao.insertChannel(channel)
    }

    suspend fun removeChannel(channelId: String) {
        sportsDao.deleteChannelById(channelId)
    }

    fun isChannelSaved(channelId: String): Flow<Boolean> {
        return sportsDao.isChannelSaved(channelId)
    }
}
