package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

class SportsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SportsDatabase.getDatabase(application)
    private val repository = SportsRepository(db.sportsDao())

    // UI state flows
    val savedMatches: StateFlow<List<SavedMatch>> = repository.savedMatches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedChannels: StateFlow<List<SavedChannel>> = repository.savedChannels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All available matches
    private val _allMatches = MutableStateFlow<List<SportMatch>>(emptyList())
    val allMatches: StateFlow<List<SportMatch>> = _allMatches.asStateFlow()

    // Filter states
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedSport = MutableStateFlow("All")
    val selectedSport: StateFlow<String> = _selectedSport.asStateFlow()

    private val _selectedStatus = MutableStateFlow("All") // "All", "LIVE", "UPCOMING", "FINISHED"
    val selectedStatus: StateFlow<String> = _selectedStatus.asStateFlow()

    // Screen navigation state / active selections
    private val _selectedMatch = MutableStateFlow<SportMatch?>(null)
    val selectedMatch: StateFlow<SportMatch?> = _selectedMatch.asStateFlow()

    private val _selectedChannel = MutableStateFlow<LiveChannel?>(null)
    val selectedChannel: StateFlow<LiveChannel?> = _selectedChannel.asStateFlow()

    // Live Channels list
    private val _channels = MutableStateFlow<List<LiveChannel>>(emptyList())
    val channels: StateFlow<List<LiveChannel>> = _channels.asStateFlow()

    // Sports News list
    private val _news = MutableStateFlow<List<NewsArticle>>(emptyList())
    val news: StateFlow<List<NewsArticle>> = _news.asStateFlow()

    // Selected News Article for detail view
    private val _selectedArticle = MutableStateFlow<NewsArticle?>(null)
    val selectedArticle: StateFlow<NewsArticle?> = _selectedArticle.asStateFlow()

    // Simulated Live Chat Flow
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    // Notification states
    private val _activeReminderToast = MutableStateFlow<String?>(null)
    val activeReminderToast: StateFlow<String?> = _activeReminderToast.asStateFlow()

    // Stream customization
    private val _selectedServer = MutableStateFlow("Server Premium FHD")
    val selectedServer: StateFlow<String> = _selectedServer.asStateFlow()

    private val _selectedQuality = MutableStateFlow("1080p (Auto)")
    val selectedQuality: StateFlow<String> = _selectedQuality.asStateFlow()

    // Active Chat simulator Job
    private var chatJob: Job? = null

    // Search and filter result combining
    val filteredMatches: StateFlow<List<SportMatch>> = combine(
        _allMatches,
        _searchQuery,
        _selectedSport,
        _selectedStatus
    ) { matches, query, sport, status ->
        matches.filter { match ->
            val matchesSearch = match.homeTeam.contains(query, ignoreCase = true) ||
                    match.awayTeam.contains(query, ignoreCase = true) ||
                    match.leagueName.contains(query, ignoreCase = true)

            val matchesSport = sport == "All" || match.sportType.equals(sport, ignoreCase = true)

            val matchesStatus = status == "All" || match.status.equals(status, ignoreCase = true)

            matchesSearch && matchesSport && matchesStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadMockData()
    }

    fun selectMatch(match: SportMatch?) {
        _selectedMatch.value = match
        _selectedChannel.value = null // clear active channel if we play a match
        _selectedServer.value = match?.servers?.firstOrNull() ?: "Server Premium FHD"
        if (match != null) {
            startChatSimulation(match.homeTeam, match.awayTeam)
        } else {
            stopChatSimulation()
        }
    }

    fun selectChannel(channel: LiveChannel?) {
        _selectedChannel.value = channel
        _selectedMatch.value = null // clear active match if we play a channel
        _selectedServer.value = "Main HLS Stream"
        if (channel != null) {
            startChatSimulation(channel.name, "Fans")
        } else {
            stopChatSimulation()
        }
    }

    fun selectArticle(article: NewsArticle?) {
        _selectedArticle.value = article
    }

    fun setServer(server: String) {
        _selectedServer.value = server
    }

    fun setQuality(quality: String) {
        _selectedQuality.value = quality
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSportFilter(sport: String) {
        _selectedSport.value = sport
    }

    fun setStatusFilter(status: String) {
        _selectedStatus.value = status
    }

    fun dismissToast() {
        _activeReminderToast.value = null
    }

    // Room Database CRUD triggers
    fun toggleMatchSaved(match: SportMatch) {
        viewModelScope.launch {
            val isCurrentlySaved = savedMatches.value.any { it.matchId == match.id }
            if (isCurrentlySaved) {
                repository.removeMatch(match.id)
                _activeReminderToast.value = "Removed ${match.homeTeam} vs ${match.awayTeam} from Watchlist."
            } else {
                val savedMatch = SavedMatch(
                    matchId = match.id,
                    homeTeam = match.homeTeam,
                    awayTeam = match.awayTeam,
                    homeLogo = match.homeLogo,
                    awayLogo = match.awayLogo,
                    leagueName = match.leagueName,
                    matchTime = match.matchTime,
                    timestamp = System.currentTimeMillis() + (if (match.status == "UPCOMING") 3600000 else 0)
                )
                repository.saveMatch(savedMatch)
                _activeReminderToast.value = if (match.status == "UPCOMING") {
                    "Reminder Scheduled! We will notify you when ${match.homeTeam} vs ${match.awayTeam} kicks off."
                } else {
                    "Added ${match.homeTeam} vs ${match.awayTeam} to Watchlist."
                }
            }
        }
    }

    fun toggleChannelSaved(channel: LiveChannel) {
        viewModelScope.launch {
            val isCurrentlySaved = savedChannels.value.any { it.channelId == channel.id }
            if (isCurrentlySaved) {
                repository.removeChannel(channel.id)
                _activeReminderToast.value = "Removed ${channel.name} from Favorites."
            } else {
                val savedChannel = SavedChannel(
                    channelId = channel.id,
                    name = channel.name,
                    logoUrl = channel.logoColorHex, // utilize color or logo representation
                    streamUrl = channel.streamUrl
                )
                repository.saveChannel(savedChannel)
                _activeReminderToast.value = "Added ${channel.name} to Favorites."
            }
        }
    }

    // Chat message simulation
    fun sendUserChatMessage(messageText: String) {
        if (messageText.isBlank()) return
        val userMsg = ChatMessage(
            id = Random.nextInt().toString(),
            sender = "You",
            avatarColorHex = "#00FF66", // electric green for user
            message = messageText
        )
        _chatMessages.value = _chatMessages.value + userMsg
    }

    private fun startChatSimulation(teamA: String, teamB: String) {
        stopChatSimulation()
        _chatMessages.value = listOf(
            ChatMessage(
                id = "sys_welcome",
                sender = "System",
                avatarColorHex = "#94A3B8",
                message = "Welcome to the Live Stream chat for $teamA vs $teamB! Enjoy the match and keep the conversation friendly.",
                isSystem = true
            )
        )

        val usernames = listOf(
            "Pierre99", "LeFootballeur", "ZizouFans", "Marseille_Fou", "PSG_Lover", 
            "UltraAllez", "SportsNut", "SpeedyG", "DribbleMaster", "RedDevil92", 
            "LaLigaBoss", "Ligue1Fanatic", "Mbappe_Jr", "TennisAce", "UFC_Warrior", "NBA_King"
        )

        val expressions = listOf(
            "Allez gooo!!! 🔥", "What a magnificent action!", "That is never a penalty! Ref is blind! 😡", 
            "Incredible defense, what a block!", "Golazoooooo!!! ⚽⚽⚽", "PSG is dominating tonight", 
            "Unbelievable save! Keeper is on fire!", "OM is looking dangerous on counter-attacks", 
            "OMG what a miss! How did he not score that??", "This match is absolute cinema",
            "Yellow card for sure!", "Is anyone else experiencing lag on Server 2? Server 1 is super smooth for me! 🙌",
            "This stream quality is crisp HD, thanks Ze Sport! ⭐", "What a tackle! Clean as a whistle.",
            "Wow! Outstanding tactical masterclass today.", "Come on!! One more goal to win it!"
        )

        val avatarColors = listOf(
            "#EF4444", "#3B82F6", "#F59E0B", "#10B981", "#8B5CF6", 
            "#EC4899", "#06B6D4", "#F43F5E", "#14B8A6", "#6366F1"
        )

        chatJob = viewModelScope.launch {
            while (true) {
                delay(Random.nextLong(2000, 5500))
                val randomMsg = ChatMessage(
                    id = Random.nextInt().toString(),
                    sender = usernames.random(),
                    avatarColorHex = avatarColors.random(),
                    message = expressions.random()
                )
                // keep chat at max 50 items
                val current = _chatMessages.value
                val updated = if (current.size > 50) current.drop(1) else current
                _chatMessages.value = updated + randomMsg
            }
        }
    }

    private fun stopChatSimulation() {
        chatJob?.cancel()
        chatJob = null
        _chatMessages.value = emptyList()
    }

    private fun loadMockData() {
        // Sample Streams:
        val stream1 = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
        val stream2 = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"
        val stream3 = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"
        val stream4 = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"

        // 1. Live TV Channels
        _channels.value = listOf(
            LiveChannel(
                id = "ch_canal_sport",
                name = "Canal+ Sport",
                logoText = "C+",
                logoColorHex = "#FF0000",
                nowPlaying = "Ligue 1 Uber Eats Match of the Day Live",
                viewersCount = "45.2k",
                category = "Premium",
                streamUrl = stream1
            ),
            LiveChannel(
                id = "ch_bein_1",
                name = "BeIN Sports 1",
                logoText = "beIN",
                logoColorHex = "#8A2BE2",
                nowPlaying = "Champions League Multi-Match Broadcast",
                viewersCount = "89.1k",
                category = "Football",
                streamUrl = stream2
            ),
            LiveChannel(
                id = "ch_rmc_1",
                name = "RMC Sport 1",
                logoText = "RMC",
                logoColorHex = "#0000FF",
                nowPlaying = "UEFA Europa League Quarterfinals Live",
                viewersCount = "31.4k",
                category = "Football",
                streamUrl = stream3
            ),
            LiveChannel(
                id = "ch_sky_main",
                name = "Sky Sports Main Event",
                logoText = "Sky",
                logoColorHex = "#FF4500",
                nowPlaying = "Premier League: Arsenal vs Chelsea LIVE",
                viewersCount = "112k",
                category = "Premium",
                streamUrl = stream4
            ),
            LiveChannel(
                id = "ch_eurosport_1",
                name = "Eurosport 1",
                logoText = "ES",
                logoColorHex = "#FFD700",
                nowPlaying = "Roland Garros: Men's Semifinals",
                viewersCount = "24.5k",
                category = "Other",
                streamUrl = stream1
            ),
            LiveChannel(
                id = "ch_dazn",
                name = "DAZN France",
                logoText = "DAZN",
                logoColorHex = "#1E293B",
                nowPlaying = "Ligue 1: Nice vs Lille Stream",
                viewersCount = "18.9k",
                category = "Football",
                streamUrl = stream2
            )
        )

        // 2. Matches Schedule
        _allMatches.value = listOf(
            SportMatch(
                id = "m1",
                homeTeam = "Paris Saint-Germain",
                awayTeam = "Olympique de Marseille",
                homeLogo = "🗼",
                awayLogo = "🔵",
                leagueName = "Ligue 1 (France)",
                leagueLogo = "⚽",
                status = "LIVE",
                matchTime = "12:00",
                score = "2 - 1",
                minute = "72'",
                streamUrl = stream3,
                sportType = "Football",
                channelName = "Canal+ Sport",
                viewersCount = "156.4k"
            ),
            SportMatch(
                id = "m2",
                homeTeam = "Real Madrid",
                awayTeam = "FC Barcelona",
                homeLogo = "👑",
                awayLogo = "🔴",
                leagueName = "La Liga (Spain)",
                leagueLogo = "⚽",
                status = "LIVE",
                matchTime = "12:15",
                score = "1 - 1",
                minute = "48'",
                streamUrl = stream1,
                sportType = "Football",
                channelName = "BeIN Sports 1",
                viewersCount = "234.1k"
            ),
            SportMatch(
                id = "m3",
                homeTeam = "Arsenal FC",
                awayTeam = "Chelsea FC",
                homeLogo = "🔴",
                awayLogo = "🔵",
                leagueName = "Premier League (England)",
                leagueLogo = "⚽",
                status = "LIVE",
                matchTime = "11:30",
                score = "3 - 0",
                minute = "89'",
                streamUrl = stream4,
                sportType = "Football",
                channelName = "Sky Sports Main Event",
                viewersCount = "92.5k"
            ),
            SportMatch(
                id = "m4",
                homeTeam = "Los Angeles Lakers",
                awayTeam = "Boston Celtics",
                homeLogo = "🏀",
                awayLogo = "🍀",
                leagueName = "NBA Regular Season",
                leagueLogo = "🏀",
                status = "LIVE",
                matchTime = "11:00",
                score = "104 - 102",
                minute = "Q4 2:14",
                streamUrl = stream2,
                sportType = "Basketball",
                channelName = "BeIN Sports 3",
                viewersCount = "81.0k"
            ),
            SportMatch(
                id = "m5",
                homeTeam = "Carlos Alcaraz",
                awayTeam = "Novak Djokovic",
                homeLogo = "🇪🇸",
                awayLogo = "🇷🇸",
                leagueName = "Wimbledon Semifinals",
                leagueLogo = "🎾",
                status = "UPCOMING",
                matchTime = "14:30",
                streamUrl = stream1,
                sportType = "Tennis",
                channelName = "Eurosport 1"
            ),
            SportMatch(
                id = "m6",
                homeTeam = "Jon Jones",
                awayTeam = "Stipe Miocic",
                homeLogo = "🥊",
                awayLogo = "🇺🇸",
                leagueName = "UFC 309 Heavyweight",
                leagueLogo = "🥊",
                status = "UPCOMING",
                matchTime = "22:00",
                streamUrl = stream3,
                sportType = "MMA",
                channelName = "RMC Sport 2"
            ),
            SportMatch(
                id = "m7",
                homeTeam = "Toulouse",
                awayTeam = "Stade Rochelais",
                homeLogo = "🔴",
                awayLogo = "🟡",
                leagueName = "Top 14 Rugby",
                leagueLogo = "🏉",
                status = "UPCOMING",
                matchTime = "17:00",
                streamUrl = stream4,
                sportType = "Rugby",
                channelName = "Canal+ Sport"
            ),
            SportMatch(
                id = "m8",
                homeTeam = "Manchester City",
                awayTeam = "Inter Milan",
                homeLogo = "🩵",
                awayLogo = "⚫",
                leagueName = "UEFA Champions League",
                leagueLogo = "⭐",
                status = "FINISHED",
                matchTime = "Yesterday",
                score = "2 - 0",
                minute = "FT",
                streamUrl = stream2,
                sportType = "Football",
                channelName = "BeIN Sports 1"
            )
        )

        // 3. Sports News
        _news.value = listOf(
            NewsArticle(
                id = "n1",
                title = "Kylian Mbappé Shines Once Again in Paris Derby Masterclass",
                category = "Football",
                timeAgo = "1 hour ago",
                imageUrl = "football_derby",
                summary = "PSG secure critical 3 points after a breathtaking 2-1 victory against Marseille, maintaining their leadership at the top of Ligue 1.",
                body = "The French capital was electrosportive tonight as Paris Saint-Germain hosted Olympique de Marseille. Kylian Mbappé was the absolute protagonist, contributing with a gorgeous volley in the 18th minute and a brilliant assist in the second half. Marseille battled valiantly, scoring in the 65th minute through a stellar counter-attack, but PSG held their ground with dynamic defensive efforts. With this win, PSG extends their lead in Ligue 1 to 7 points."
            ),
            NewsArticle(
                id = "n2",
                title = "Lakers Clinch Historic Double-Overtime Win Over Celtics",
                category = "Basketball",
                timeAgo = "3 hours ago",
                imageUrl = "nba_lakers",
                summary = "In one of the most legendary regular-season match-ups, the Lakers edge past Celtics in a 118-115 absolute classic.",
                body = "It felt like an NBA Finals game in July. The Lakers and Celtics wrote another dramatic chapter in their historic rivalry. After finishing regulation tied at 98, both teams traded heavy blows in the first overtime. It was LeBron James who secured the victory in the second overtime with two clutches from beyond the arc. Anthony Davis dominated the paint with 32 points and 16 rebounds, while Jayson Tatum's heroic 41 points for Boston fell just short."
            ),
            NewsArticle(
                id = "n3",
                title = "Alcaraz vs Djokovic: Wimbledon Final Matchup Declared",
                category = "Tennis",
                timeAgo = "5 hours ago",
                imageUrl = "tennis_final",
                summary = "The stage is set for a historic Wimbledon Men's Singles final on Sunday, matching the world's finest tennis stars.",
                body = "Carlos Alcaraz and Novak Djokovic will collide in a highly anticipated dream final on Centre Court. Alcaraz cruised through his semifinal with a brilliant straight-sets victory, showing unparalleled speed and precision on the grass surface. Meanwhile, Djokovic survived a tough four-set encounter against Jannik Sinner to reach his tenth Wimbledon final. Tennis fans around the world are preparing for what promises to be an epic tactical battle."
            )
        )
    }
}
