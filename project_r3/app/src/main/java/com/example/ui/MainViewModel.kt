package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val preferencesManager = PreferencesManager(context)
    private val firebaseRepository = FirebaseRepository(context)
    val supabasePresenceManager = SupabasePresenceManager()

    // Authentication States
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _isLoggingIn = MutableStateFlow(false)
    val isLoggingIn: StateFlow<Boolean> = _isLoggingIn

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError

    // Configurations state
    val themeAccent: StateFlow<String> = preferencesManager.selectedTheme.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "Neon"
    )

    val playerMode: StateFlow<String> = preferencesManager.playerMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "Smart"
    )

    val appLanguage: StateFlow<String> = preferencesManager.appLanguage.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "ar"
    )

    val gridColumns: StateFlow<Int> = preferencesManager.gridColumns.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 3
    )

    val isLandscapeMode: StateFlow<Boolean> = preferencesManager.isLandscapeMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    // active playlist details
    private var xtreamService: XtreamService? = null
    private var activeHostState = ""
    private var activeUsernameState = ""
    private var activePasswordState = ""

    // Current Navigation states
    private val _activeTab = MutableStateFlow("live") // live, movies, series, favorites, settings
    val activeTab: StateFlow<String> = _activeTab

    // Video Player state
    private val _selectedStreamUrl = MutableStateFlow<String?>(null)
    val selectedStreamUrl: StateFlow<String?> = _selectedStreamUrl

    private val _selectedStreamName = MutableStateFlow<String?>(null)
    val selectedStreamName: StateFlow<String?> = _selectedStreamName

    // Loading indicator for IPTV lists
    private val _isContentLoading = MutableStateFlow(false)
    val isContentLoading: StateFlow<Boolean> = _isContentLoading

    // Dynamic Categories retrieved from Xtream API
    private val _liveCategories = MutableStateFlow<List<XtreamCategory>>(emptyList())
    val liveCategories: StateFlow<List<XtreamCategory>> = _liveCategories

    private val _movieCategories = MutableStateFlow<List<XtreamCategory>>(emptyList())
    val movieCategories: StateFlow<List<XtreamCategory>> = _movieCategories

    private val _seriesCategories = MutableStateFlow<List<XtreamCategory>>(emptyList())
    val seriesCategories: StateFlow<List<XtreamCategory>> = _seriesCategories

    // Selected category ID for each source
    private val _selectedLiveCategoryId = MutableStateFlow<String?>(null)
    val selectedLiveCategoryId: StateFlow<String?> = _selectedLiveCategoryId

    private val _selectedMovieCategoryId = MutableStateFlow<String?>(null)
    val selectedMovieCategoryId: StateFlow<String?> = _selectedMovieCategoryId

    private val _selectedSeriesCategoryId = MutableStateFlow<String?>(null)
    val selectedSeriesCategoryId: StateFlow<String?> = _selectedSeriesCategoryId

    // Fetched streams lists
    private val _liveStreams = MutableStateFlow<List<LiveStreamItem>>(emptyList())
    val liveStreams: StateFlow<List<LiveStreamItem>> = _liveStreams

    private val _movieStreams = MutableStateFlow<List<MovieStreamItem>>(emptyList())
    val movieStreams: StateFlow<List<MovieStreamItem>> = _movieStreams

    private val _seriesItems = MutableStateFlow<List<SeriesItem>>(emptyList())
    val seriesItems: StateFlow<List<SeriesItem>> = _seriesItems

    // Star/Favorites
    val favoriteLiveIds: StateFlow<Set<String>> = preferencesManager.favoriteLiveStreams.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptySet()
    )

    val favoriteMovieIds: StateFlow<Set<String>> = preferencesManager.favoriteMovies.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptySet()
    )

    val favoriteSeriesIds: StateFlow<Set<String>> = preferencesManager.favoriteSeries.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptySet()
    )

    // Series detail state
    private val _activeSeriesEpisodes = MutableStateFlow<List<SeriesEpisode>>(emptyList())
    val activeSeriesEpisodes: StateFlow<List<SeriesEpisode>> = _activeSeriesEpisodes

    private val _selectedSeries = MutableStateFlow<SeriesItem?>(null)
    val selectedSeries: StateFlow<SeriesItem?> = _selectedSeries

    init {
        // Start Presence tracker
        supabasePresenceManager.start()

        // Check if there are active saved credentials for automatic logs
        viewModelScope.launch {
            preferencesManager.isLoggedIn.distinctUntilChanged().collect { loggedIn ->
                _isLoggedIn.value = loggedIn
                if (loggedIn) {
                    preferencesManager.credentials.distinctUntilChanged().take(1).collect { triple ->
                        if (triple != null) {
                            activeHostState = triple.first
                            activeUsernameState = triple.second
                            activePasswordState = triple.third
                            xtreamService = XtreamClientBuilder.create(activeHostState)
                            // Load initial live categories
                            fetchTabCategories("live")
                        }
                    }
                }
            }
        }
    }

    fun login(code: String) {
        if (code.trim().isEmpty()) {
            _loginError.value = "الرجاء إدخال كود التفعيل"
            return
        }

        _isLoggingIn.value = true
        _loginError.value = null

        viewModelScope.launch {
            val credentials = firebaseRepository.fetchCredentials(code.trim())
            if (credentials != null) {
                // Save credentials locally
                preferencesManager.saveCredentials(
                    host = credentials.first,
                    username = credentials.second,
                    password = credentials.third
                )
                activeHostState = credentials.first
                activeUsernameState = credentials.second
                activePasswordState = credentials.third
                xtreamService = XtreamClientBuilder.create(activeHostState)
                
                _isLoggedIn.value = true
                _loginError.value = null
                fetchTabCategories("live")
            } else {
                _loginError.value = "كود تفعيل غير صحيح أو منتهي الصلاحية"
            }
            _isLoggingIn.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            preferencesManager.clearSession()
            xtreamService = null
            activeHostState = ""
            activeUsernameState = ""
            activePasswordState = ""
            _isLoggedIn.value = false
            _liveCategories.value = emptyList()
            _movieCategories.value = emptyList()
            _seriesCategories.value = emptyList()
            _liveStreams.value = emptyList()
            _movieStreams.value = emptyList()
            _seriesItems.value = emptyList()
            _selectedLiveCategoryId.value = null
            _selectedMovieCategoryId.value = null
            _selectedSeriesCategoryId.value = null
        }
    }

    fun setTab(tab: String) {
        _activeTab.value = tab
        _selectedSeries.value = null
        _activeSeriesEpisodes.value = emptyList()
        viewModelScope.launch {
            fetchTabCategories(tab)
        }
    }

    fun selectLiveCategory(categoryId: String) {
        _selectedLiveCategoryId.value = categoryId
        fetchLiveStreams(categoryId)
    }

    fun selectMovieCategory(categoryId: String) {
        _selectedMovieCategoryId.value = categoryId
        fetchMovieStreams(categoryId)
    }

    fun selectSeriesCategory(categoryId: String) {
        _selectedSeriesCategoryId.value = categoryId
        fetchSeries(categoryId)
    }

    fun setPlayerTheme(accent: String) {
        viewModelScope.launch {
            preferencesManager.saveTheme(accent)
        }
    }

    fun setAppLanguage(lang: String) {
        viewModelScope.launch {
            preferencesManager.saveAppLanguage(lang)
        }
    }

    fun setGridColumns(cols: Int) {
        viewModelScope.launch {
            preferencesManager.saveGridColumns(cols)
        }
    }

    fun setPlayerTypeMode(mode: String) {
        viewModelScope.launch {
            preferencesManager.savePlayerMode(mode)
        }
    }

    fun setLandscapeMode(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.saveLandscapeMode(enabled)
        }
    }

    // Toggle Favorites
    fun toggleFavoriteLive(itemId: String) {
        viewModelScope.launch { preferencesManager.toggleFavoriteLive(itemId) }
    }

    fun toggleFavoriteMovie(itemId: String) {
        viewModelScope.launch { preferencesManager.toggleFavoriteMovie(itemId) }
    }

    fun toggleFavoriteSeries(itemId: String) {
        viewModelScope.launch { preferencesManager.toggleFavoriteSeries(itemId) }
    }

    // Load Live Streams of selected category
    private fun fetchLiveStreams(categoryId: String) {
        val service = xtreamService ?: return
        _isContentLoading.value = true
        _liveStreams.value = emptyList()
        viewModelScope.launch {
            try {
                val streams = if (categoryId == "all") {
                    service.getLiveStreams(
                        username = activeUsernameState,
                        password = activePasswordState
                    )
                } else {
                    service.getLiveStreamsByCategory(
                        username = activeUsernameState,
                        password = activePasswordState,
                        categoryId = categoryId
                    )
                }
                _liveStreams.value = streams
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error live streams: ${e.message}")
            } finally {
                _isContentLoading.value = false
            }
        }
    }

    // Load VOD streams
    private fun fetchMovieStreams(categoryId: String) {
        val service = xtreamService ?: return
        _isContentLoading.value = true
        _movieStreams.value = emptyList()
        viewModelScope.launch {
            try {
                val movies = if (categoryId == "all") {
                    service.getVodStreams(
                        username = activeUsernameState,
                        password = activePasswordState
                    )
                } else {
                    service.getVodStreamsByCategory(
                        username = activeUsernameState,
                        password = activePasswordState,
                        categoryId = categoryId
                    )
                }
                _movieStreams.value = movies
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error movies streams: ${e.message}")
            } finally {
                _isContentLoading.value = false
            }
        }
    }

    // Load Series
    private fun fetchSeries(categoryId: String) {
        val service = xtreamService ?: return
        _isContentLoading.value = true
        _seriesItems.value = emptyList()
        viewModelScope.launch {
            try {
                val series = if (categoryId == "all") {
                    service.getSeries(
                        username = activeUsernameState,
                        password = activePasswordState
                    )
                } else {
                    service.getSeriesByCategory(
                        username = activeUsernameState,
                        password = activePasswordState,
                        categoryId = categoryId
                    )
                }
                _seriesItems.value = series
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error series: ${e.message}")
            } finally {
                _isContentLoading.value = false
            }
        }
    }

    // Open Series details and load episodes
    fun selectSeriesDetails(series: SeriesItem) {
        _selectedSeries.value = series
        _isContentLoading.value = true
        _activeSeriesEpisodes.value = emptyList()
        val service = xtreamService ?: return
        viewModelScope.launch {
            try {
                val response = service.getSeriesInfo(
                    username = activeUsernameState,
                    password = activePasswordState,
                    seriesId = series.seriesId
                )
                // Flatten map of season -> episodes list
                val episodesList = response.episodes?.values?.flatten() ?: emptyList()
                _activeSeriesEpisodes.value = episodesList
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error fetching series info: ${e.message}")
            } finally {
                _isContentLoading.value = false
            }
        }
    }

    fun closeSeriesDetails() {
        _selectedSeries.value = null
        _activeSeriesEpisodes.value = emptyList()
    }

    // Dynamically retrieve URL format based on play settings (Smart, HLS, TS, etc.)
    fun selectLivePlayback(itemId: Int, itemName: String) {
        // Construct standard IPTV play URL: http://<host>/live/<username>/<password>/<stream_id>.ts
        val cleanHost = activeHostState.trimEnd('/')
        val url = "$cleanHost/live/$activeUsernameState/$activePasswordState/$itemId.ts"
        _selectedStreamName.value = itemName
        _selectedStreamUrl.value = url
    }

    fun selectMoviePlayback(itemId: Int, container: String?, itemName: String) {
        val cleanHost = activeHostState.trimEnd('/')
        val ext = container ?: "mp4"
        val url = "$cleanHost/movie/$activeUsernameState/$activePasswordState/$itemId.$ext"
        _selectedStreamName.value = itemName
        _selectedStreamUrl.value = url
    }

    fun selectEpisodePlayback(episodeId: String, container: String?, episodeTitle: String) {
        val cleanHost = activeHostState.trimEnd('/')
        val ext = container ?: "mp4"
        val url = "$cleanHost/series/$activeUsernameState/$activePasswordState/$episodeId.$ext"
        _selectedStreamName.value = episodeTitle
        _selectedStreamUrl.value = url
    }

    fun clearPlayback() {
        _selectedStreamUrl.value = null
        _selectedStreamName.value = null
    }

    // Fetch horizontal category items for specified tab
    private suspend fun fetchTabCategories(tab: String) {
        val service = xtreamService ?: return
        when (tab) {
            "live" -> {
                if (_liveCategories.value.isNotEmpty()) return
                _isContentLoading.value = true
                try {
                    val cats = service.getLiveCategories(activeUsernameState, activePasswordState)
                    val fullCats = listOf(XtreamCategory("all", "الكل", 0)) + cats
                    _liveCategories.value = fullCats
                    if (fullCats.isNotEmpty() && _selectedLiveCategoryId.value == null) {
                        selectLiveCategory("all")
                    }
                } catch (e: Exception) {
                    Log.e("MainViewModel", "Error live categories: ${e.message}")
                } finally {
                    _isContentLoading.value = false
                }
            }
            "movies" -> {
                if (_movieCategories.value.isNotEmpty()) return
                _isContentLoading.value = true
                try {
                    val cats = service.getVodCategories(activeUsernameState, activePasswordState)
                    val fullCats = listOf(XtreamCategory("all", "الكل", 0)) + cats
                    _movieCategories.value = fullCats
                    if (fullCats.isNotEmpty() && _selectedMovieCategoryId.value == null) {
                        selectMovieCategory("all")
                    }
                } catch (e: Exception) {
                    Log.e("MainViewModel", "Error movies categories: ${e.message}")
                } finally {
                    _isContentLoading.value = false
                }
            }
            "series" -> {
                if (_seriesCategories.value.isNotEmpty()) return
                _isContentLoading.value = true
                try {
                    val cats = service.getSeriesCategories(activeUsernameState, activePasswordState)
                    val fullCats = listOf(XtreamCategory("all", "الكل", 0)) + cats
                    _seriesCategories.value = fullCats
                    if (fullCats.isNotEmpty() && _selectedSeriesCategoryId.value == null) {
                        selectSeriesCategory("all")
                    }
                } catch (e: Exception) {
                    Log.e("MainViewModel", "Error series categories: ${e.message}")
                } finally {
                    _isContentLoading.value = false
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        supabasePresenceManager.stop()
    }
}
