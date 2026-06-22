package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import android.content.res.Configuration
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.LiveStreamItem
import com.example.data.MovieStreamItem
import com.example.data.SeriesItem
import com.example.data.XtreamCategory
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToDeveloper: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activeTab by viewModel.activeTab.collectAsState()
    val isContentLoading by viewModel.isContentLoading.collectAsState()
    val isLandscapeMode by viewModel.isLandscapeMode.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val configuration = LocalConfiguration.current
    val isLandscape = isLandscapeMode || (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)

    // Unified Responsive Layout
    Scaffold(
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isLandscape) 44.dp else 50.dp)
                        .background(Color(0xFF07080A))
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "LOOP LIVE",
                            fontWeight = FontWeight.Black,
                            fontSize = if (isLandscape) 16.sp else 18.sp,
                            letterSpacing = 2.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (appLanguage == "ar") "PRO" else "PRO",
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Contact developer Telegram shortcut
                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/jdj_q"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier
                                .background(Color(0xFF229ED9).copy(alpha = 0.15f), CircleShape)
                                .size(if (isLandscape) 28.dp else 34.dp)
                        ) {
                            Icon(
                                imageVector = CustomIcons.Telegram,
                                contentDescription = "Telegram Support",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(if (isLandscape) 12.dp else 16.dp)
                            )
                        }

                        // Exit logout buttons
                        IconButton(
                            onClick = { viewModel.logout() },
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.05f), CircleShape)
                                .size(if (isLandscape) 28.dp else 34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Sign Out",
                                tint = Color.Red,
                                modifier = Modifier.size(if (isLandscape) 12.dp else 16.dp)
                            )
                        }
                    }
                }
            },
            bottomBar = {
                if (!isLandscape) {
                    // High premium TOD-Style Bottom Navigation Bar
                    NavigationBar(
                        containerColor = Color(0xFF060709),
                        tonalElevation = 0.dp,
                        modifier = Modifier
                            .border(width = 1.dp, color = Color.White.copy(alpha = 0.05f))
                            .height(68.dp)
                    ) {
                        val accentColor = MaterialTheme.colorScheme.primary

                        // 1. Live TV tab
                        NavigationBarItem(
                            selected = activeTab == "live",
                            onClick = { viewModel.setTab("live") },
                            icon = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = if (activeTab == "live") Icons.Filled.LiveTv else Icons.Outlined.LiveTv,
                                        contentDescription = null,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    if (activeTab == "live") {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(modifier = Modifier.size(4.dp).background(accentColor, CircleShape))
                                    }
                                }
                            },
                            label = { Text(LocaleHelper.translate("live", appLanguage), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = accentColor,
                                selectedTextColor = accentColor,
                                indicatorColor = Color.Transparent,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            )
                        )

                        // 2. Movies tab
                        NavigationBarItem(
                            selected = activeTab == "movies",
                            onClick = { viewModel.setTab("movies") },
                            icon = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = if (activeTab == "movies") Icons.Filled.Movie else Icons.Outlined.Movie,
                                        contentDescription = null,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    if (activeTab == "movies") {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(modifier = Modifier.size(4.dp).background(accentColor, CircleShape))
                                    }
                                }
                            },
                            label = { Text(LocaleHelper.translate("movies", appLanguage), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = accentColor,
                                selectedTextColor = accentColor,
                                indicatorColor = Color.Transparent,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            )
                        )

                        // 3. Series Tab
                        NavigationBarItem(
                            selected = activeTab == "series",
                            onClick = { viewModel.setTab("series") },
                            icon = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = if (activeTab == "series") Icons.Filled.Tv else Icons.Outlined.Tv,
                                        contentDescription = null,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    if (activeTab == "series") {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(modifier = Modifier.size(4.dp).background(accentColor, CircleShape))
                                    }
                                }
                            },
                            label = { Text(LocaleHelper.translate("series", appLanguage), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = accentColor,
                                selectedTextColor = accentColor,
                                indicatorColor = Color.Transparent,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            )
                        )

                        // 4. Favorites Tab
                        NavigationBarItem(
                            selected = activeTab == "favorites",
                            onClick = { viewModel.setTab("favorites") },
                            icon = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = if (activeTab == "favorites") Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                        contentDescription = null,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    if (activeTab == "favorites") {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(modifier = Modifier.size(4.dp).background(accentColor, CircleShape))
                                    }
                                }
                            },
                            label = { Text(LocaleHelper.translate("favorites", appLanguage), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = accentColor,
                                selectedTextColor = accentColor,
                                indicatorColor = Color.Transparent,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            )
                        )

                        // 5. Settings Tab
                        NavigationBarItem(
                            selected = activeTab == "settings",
                            onClick = { viewModel.setTab("settings") },
                            icon = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = if (activeTab == "settings") Icons.Filled.Settings else Icons.Outlined.Settings,
                                        contentDescription = null,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    if (activeTab == "settings") {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(modifier = Modifier.size(4.dp).background(accentColor, CircleShape))
                                    }
                                }
                            },
                            label = { Text(LocaleHelper.translate("settings", appLanguage), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = accentColor,
                                selectedTextColor = accentColor,
                                indicatorColor = Color.Transparent,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            )
                        )
                    }
                }
            },
            containerColor = DarkBackground,
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isLandscape) {
                    SideNavigationRail(
                        viewModel = viewModel,
                        activeTab = activeTab
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    // Tab router content
                    when (activeTab) {
                        "live" -> LiveTabContent(viewModel)
                        "movies" -> MoviesTabContent(viewModel)
                        "series" -> SeriesTabContent(viewModel)
                        "favorites" -> FavoritesTabContent(viewModel)
                        "settings" -> SettingsTabContent(viewModel, onNavigateToDeveloper)
                    }

                    // Beautiful Custom Premium Dual-Orbit Loading Spinner
                    if (isContentLoading) {
                        PremiumCircularLoader()
                    }
                }
            }
        }
}

@Composable
fun SideNavigationRail(
    viewModel: MainViewModel,
    activeTab: String,
    modifier: Modifier = Modifier
) {
    val accentColor = MaterialTheme.colorScheme.primary
    val appLanguage by viewModel.appLanguage.collectAsState()

    Row(
        modifier = modifier
            .fillMaxHeight()
            .width(80.dp)
            .background(Color(0xFF060709))
            .border(width = 1.dp, color = Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val items = listOf(
                Triple("live", LocaleHelper.translate("live", appLanguage), if (activeTab == "live") Icons.Filled.LiveTv else Icons.Outlined.LiveTv),
                Triple("movies", LocaleHelper.translate("movies", appLanguage), if (activeTab == "movies") Icons.Filled.Movie else Icons.Outlined.Movie),
                Triple("series", LocaleHelper.translate("series", appLanguage), if (activeTab == "series") Icons.Filled.Tv else Icons.Outlined.Tv),
                Triple("favorites", LocaleHelper.translate("favorites", appLanguage), if (activeTab == "favorites") Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder),
                Triple("settings", LocaleHelper.translate("settings", appLanguage), if (activeTab == "settings") Icons.Filled.Settings else Icons.Outlined.Settings)
            )

            items.forEach { (route, label, icon) ->
                val isSelected = activeTab == route
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.setTab(route) }
                        .padding(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) accentColor else Color.Gray,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = label,
                        color = if (isSelected) accentColor else Color.Gray,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Vertical divider line
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
                .background(Color(0xFF1B1E26))
        )
    }
}

// ---------------------- PRESET PREMIUM CUSTOM SPINNER ----------------------
@Composable
fun PremiumCircularLoader() {
    val infiniteTransition = rememberInfiniteTransition(label = "loader_cycles")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_spin"
    )
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orbit_pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.82f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(100.dp)
            ) {
                // Outer rotating ring
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 3.dp,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationZ = angle }
                )
                // Inner rotating reversed pulsing ring
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.secondary,
                    strokeWidth = 1.5.dp,
                    modifier = Modifier
                        .size(68.dp)
                        .graphicsLayer {
                            rotationZ = -angle
                            scaleX = scale
                            scaleY = scale
                        }
                )
                // Centered glowing play brand icon symbol
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "جاري الاتصال وسحب البيانات بسرعة البرق...",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "LOOP LIVE NETWORKS",
                color = Color.Gray,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

// ---------------------- LIVE TAB CONTENT ----------------------
@Composable
fun LiveTabContent(viewModel: MainViewModel) {
    val categories by viewModel.liveCategories.collectAsState()
    val selectedCategory by viewModel.selectedLiveCategoryId.collectAsState()
    val streams by viewModel.liveStreams.collectAsState()
    val favorites by viewModel.favoriteLiveIds.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val gridCols by viewModel.gridColumns.collectAsState()
    val isContentLoading by viewModel.isContentLoading.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    val filteredStreams = remember(streams, searchQuery) {
        if (searchQuery.isBlank()) {
            streams
        } else {
            streams.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CategoriesRow(
            categories = categories,
            selectedCategoryId = selectedCategory,
            onSelect = { viewModel.selectLiveCategory(it) }
        )

        // Custom Compact Search Row for Live TV
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    text = LocaleHelper.translate("search_live", appLanguage),
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                unfocusedBorderColor = Color.White.copy(alpha = 0.05f),
                focusedContainerColor = Color(0xFF131519),
                unfocusedContainerColor = Color(0xFF131519),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .height(48.dp)
        )

        if (filteredStreams.isEmpty()) {
            if (isContentLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp
                    )
                }
            } else {
                EmptyStatePlaceholder(message = if (searchQuery.isNotBlank()) LocaleHelper.translate("no_results", appLanguage) else LocaleHelper.translate("empty_live", appLanguage))
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(gridCols),
                contentPadding = PaddingValues(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredStreams) { stream ->
                    val isFav = favorites.contains(stream.streamId.toString())
                    LiveItemCard(
                        stream = stream,
                        isFavorite = isFav,
                        onToggleFavorite = { viewModel.toggleFavoriteLive(stream.streamId.toString()) },
                        onClick = { viewModel.selectLivePlayback(stream.streamId, stream.name) }
                    )
                }
            }
        }
    }
}

// ---------------------- MOVIES TAB CONTENT (NETFLIX STYLE) ----------------------
@Composable
fun MoviesTabContent(viewModel: MainViewModel) {
    val categories by viewModel.movieCategories.collectAsState()
    val selectedCategory by viewModel.selectedMovieCategoryId.collectAsState()
    val movies by viewModel.movieStreams.collectAsState()
    val favorites by viewModel.favoriteMovieIds.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val gridCols by viewModel.gridColumns.collectAsState()
    val isContentLoading by viewModel.isContentLoading.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    val filteredMovies = remember(movies, searchQuery) {
        if (searchQuery.isBlank()) {
            movies
        } else {
            movies.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            CategoriesRow(
                categories = categories,
                selectedCategoryId = selectedCategory,
                onSelect = { viewModel.selectMovieCategory(it) }
            )
        }

        item {
            // Custom Compact Search Row for Movies
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = LocaleHelper.translate("search_movies", appLanguage),
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.05f),
                    focusedContainerColor = Color(0xFF131519),
                    unfocusedContainerColor = Color(0xFF131519),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(48.dp)
            )
        }

        if (filteredMovies.isEmpty()) {
            if (isContentLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp
                        )
                    }
                }
            } else {
                item {
                    EmptyStatePlaceholder(message = if (searchQuery.isNotBlank()) LocaleHelper.translate("no_results", appLanguage) else LocaleHelper.translate("empty_movies", appLanguage))
                }
            }
        } else {
            // Netflix Billboard Banner (Top Featured Slide)
            val featuredMovie = filteredMovies.firstOrNull()
            if (featuredMovie != null && searchQuery.isBlank()) {
                item {
                    MovieBillboard(
                        title = featuredMovie.name,
                        iconUrl = featuredMovie.streamIcon,
                        isFavorite = favorites.contains(featuredMovie.streamId.toString()),
                        onPlay = { viewModel.selectMoviePlayback(featuredMovie.streamId, featuredMovie.containerExtension, featuredMovie.name) },
                        onToggleFavorite = { viewModel.toggleFavoriteMovie(featuredMovie.streamId.toString()) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))

                // Section Title Label
                Text(
                    text = LocaleHelper.translate("recommended_movies", appLanguage),
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
                )
            }

            // Netflix vertical content gallery row flow
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                ) {
                    items(filteredMovies) { movie ->
                        val isFav = favorites.contains(movie.streamId.toString())
                        MovieItemCard(
                            movie = movie,
                            isFavorite = isFav,
                            onToggleFavorite = { viewModel.toggleFavoriteMovie(movie.streamId.toString()) },
                            onClick = { viewModel.selectMoviePlayback(movie.streamId, movie.containerExtension, movie.name) },
                            modifier = Modifier.width(134.dp)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))

                // Grid below of other movies
                Text(
                    text = LocaleHelper.translate("all_movies", appLanguage),
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
                )
            }

            // Dynamic grid layout
            val movieChunks = filteredMovies.chunked(gridCols)
            items(movieChunks) { chunk ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    chunk.forEach { movie ->
                        val isFav = favorites.contains(movie.streamId.toString())
                        MovieItemCard(
                            movie = movie,
                            isFavorite = isFav,
                            onToggleFavorite = { viewModel.toggleFavoriteMovie(movie.streamId.toString()) },
                            onClick = { viewModel.selectMoviePlayback(movie.streamId, movie.containerExtension, movie.name) },
                            modifier = Modifier
                                .weight(1f)
                                .padding(4.dp)
                        )
                    }
                    // Fill extra weights if row is incomplete
                    if (chunk.size < gridCols) {
                        repeat(gridCols - chunk.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

// ---------------------- SERIES TAB CONTENT (NETFLIX STYLE) ----------------------
@Composable
fun SeriesTabContent(viewModel: MainViewModel) {
    val categories by viewModel.seriesCategories.collectAsState()
    val selectedCategory by viewModel.selectedSeriesCategoryId.collectAsState()
    val seriesList by viewModel.seriesItems.collectAsState()
    val selectedSeries by viewModel.selectedSeries.collectAsState()
    val episodes by viewModel.activeSeriesEpisodes.collectAsState()
    val favorites by viewModel.favoriteSeriesIds.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val gridCols by viewModel.gridColumns.collectAsState()
    val isContentLoading by viewModel.isContentLoading.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    val filteredSeries = remember(seriesList, searchQuery) {
        if (searchQuery.isBlank()) {
            seriesList
        } else {
            seriesList.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                CategoriesRow(
                    categories = categories,
                    selectedCategoryId = selectedCategory,
                    onSelect = { viewModel.selectSeriesCategory(it) }
                )
            }

            item {
                // Custom Compact Search Row for Series
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = LocaleHelper.translate("search_series", appLanguage),
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.05f),
                        focusedContainerColor = Color(0xFF131519),
                        unfocusedContainerColor = Color(0xFF131519),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(48.dp)
                )
            }

            if (filteredSeries.isEmpty()) {
                if (isContentLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 3.dp
                            )
                        }
                    }
                } else {
                    item {
                        EmptyStatePlaceholder(message = if (searchQuery.isNotBlank()) LocaleHelper.translate("no_results", appLanguage) else LocaleHelper.translate("empty_series", appLanguage))
                    }
                }
            } else {
                // Netflix Feature billboard slide
                val featuredSeries = filteredSeries.firstOrNull()
                if (featuredSeries != null && searchQuery.isBlank()) {
                    item {
                        MovieBillboard(
                            title = featuredSeries.name,
                            iconUrl = featuredSeries.cover,
                            isFavorite = favorites.contains(featuredSeries.seriesId.toString()),
                            onPlay = { viewModel.selectSeriesDetails(featuredSeries) },
                            onToggleFavorite = { viewModel.toggleFavoriteSeries(featuredSeries.seriesId.toString()) },
                            playLabel = LocaleHelper.translate("explore_series", appLanguage)
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = LocaleHelper.translate("recommended_series", appLanguage),
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
                    )
                }

                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    ) {
                        items(filteredSeries) { series ->
                            val isFav = favorites.contains(series.seriesId.toString())
                            SeriesItemCard(
                                series = series,
                                isFavorite = isFav,
                                onToggleFavorite = { viewModel.toggleFavoriteSeries(series.seriesId.toString()) },
                                onClick = { viewModel.selectSeriesDetails(series) },
                                modifier = Modifier.width(134.dp)
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = LocaleHelper.translate("all_series", appLanguage),
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
                    )
                }

                // Grid Layout inside LazyColumn based on user chosen columns list
                val seriesChunks = filteredSeries.chunked(gridCols)
                items(seriesChunks) { chunk ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        chunk.forEach { series ->
                            val isFav = favorites.contains(series.seriesId.toString())
                            SeriesItemCard(
                                series = series,
                                isFavorite = isFav,
                                onToggleFavorite = { viewModel.toggleFavoriteSeries(series.seriesId.toString()) },
                                onClick = { viewModel.selectSeriesDetails(series) },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(4.dp)
                            )
                        }
                        if (chunk.size < gridCols) {
                            repeat(gridCols - chunk.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // Series Details overlay dialogsheet
        AnimatedVisibility(
            visible = selectedSeries != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            selectedSeries?.let { series ->
                SeriesDetailsOverlay(
                    series = series,
                    episodes = episodes,
                    onClose = { viewModel.closeSeriesDetails() },
                    onEpisodeClick = { episode ->
                        viewModel.selectEpisodePlayback(
                            episode.id,
                            episode.containerExtension,
                            "${series.name} - حلقة ${episode.episodeNum}"
                        )
                    }
                )
            }
        }
    }
}

// ---------------------- NETFLIX BILLBOARD COMPOSE BANNER ----------------------
@Composable
fun MovieBillboard(
    title: String,
    iconUrl: String?,
    isFavorite: Boolean,
    onPlay: () -> Unit,
    onToggleFavorite: () -> Unit,
    playLabel: String = "تشغيل الآن"
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0B0E)),
        modifier = Modifier
            .fillMaxWidth()
            .height(290.dp)
            .padding(16.dp)
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Poster Background Stretch
            if (!iconUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = iconUrl,
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // High gradient Overlay to cover bottom safely with black transparency
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.4f),
                                Color.Black.copy(alpha = 0.95f)
                            )
                        )
                    )
            )

            // Metadata items
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Glow badge
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Red)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("متميز 🔥", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "عالي الجودة UHD",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Play / actions row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onPlay,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        modifier = Modifier.height(42.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(playLabel, color = Color.Black, fontWeight = FontWeight.Black, fontSize = 13.sp)
                        }
                    }

                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                            .size(42.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = if (isFavorite) Color.Red else Color.White
                        )
                    }
                }
            }
        }
    }
}

// ---------------------- FAVORITES TAB CONTENT ----------------------
@Composable
fun FavoritesTabContent(viewModel: MainViewModel) {
    val liveStreams by viewModel.liveStreams.collectAsState()
    val favoriteLiveIds by viewModel.favoriteLiveIds.collectAsState()

    val moviesStreams by viewModel.movieStreams.collectAsState()
    val favoriteMovieIds by viewModel.favoriteMovieIds.collectAsState()

    val seriesList by viewModel.seriesItems.collectAsState()
    val favoriteSeriesIds by viewModel.favoriteSeriesIds.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()

    val favLiveItems = liveStreams.filter { favoriteLiveIds.contains(it.streamId.toString()) }
    val favMovieItems = moviesStreams.filter { favoriteMovieIds.contains(it.streamId.toString()) }
    val favSeriesItems = seriesList.filter { favoriteSeriesIds.contains(it.seriesId.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = LocaleHelper.translate("favorite_channels", appLanguage),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (favLiveItems.isEmpty()) {
            Text(
                text = LocaleHelper.translate("empty_fav_channels", appLanguage),
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                items(favLiveItems) { stream ->
                    LiveItemCard(
                        stream = stream,
                        isFavorite = true,
                        onToggleFavorite = { viewModel.toggleFavoriteLive(stream.streamId.toString()) },
                        onClick = { viewModel.selectLivePlayback(stream.streamId, stream.name) },
                        modifier = Modifier.width(110.dp)
                    )
                }
            }
        }

        Text(
            text = LocaleHelper.translate("favorite_movies", appLanguage),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (favMovieItems.isEmpty()) {
            Text(
                text = LocaleHelper.translate("empty_fav_movies", appLanguage),
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                items(favMovieItems) { movie ->
                    MovieItemCard(
                        movie = movie,
                        isFavorite = true,
                        onToggleFavorite = { viewModel.toggleFavoriteMovie(movie.streamId.toString()) },
                        onClick = { viewModel.selectMoviePlayback(movie.streamId, movie.containerExtension, movie.name) },
                        modifier = Modifier.width(134.dp)
                    )
                }
            }
        }

        Text(
            text = LocaleHelper.translate("favorite_series", appLanguage),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (favSeriesItems.isEmpty()) {
            Text(
                text = LocaleHelper.translate("empty_fav_series", appLanguage),
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(favSeriesItems) { series ->
                    SeriesItemCard(
                        series = series,
                        isFavorite = true,
                        onToggleFavorite = { viewModel.toggleFavoriteSeries(series.seriesId.toString()) },
                        onClick = { viewModel.selectSeriesDetails(series) },
                        modifier = Modifier.width(134.dp)
                    )
                }
            }
        }
    }
}

// ---------------------- SETTINGS TAB CONTENT (POLISHED DIALOG SHEETS) ----------------------
@Composable
fun SettingsTabContent(
    viewModel: MainViewModel,
    onNavigateToDeveloper: () -> Unit
) {
    val activeTheme by viewModel.themeAccent.collectAsState()
    val activePlayerMode by viewModel.playerMode.collectAsState()
    val isLandscapeMode by viewModel.isLandscapeMode.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val gridColumns by viewModel.gridColumns.collectAsState()

    var showAppearanceDialog by remember { mutableStateOf(false) }
    var showPlayerDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showGridDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = LocaleHelper.translate("general_settings", appLanguage),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            textAlign = TextAlign.Start
        )

        // 1. Appearance selection trigger card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0B0E)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showAppearanceDialog = true }
                .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = null,
                    tint = Color.Gray
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = LocaleHelper.translate("app_theme", appLanguage),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = LocaleHelper.translate("current_theme", appLanguage) + activeTheme,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // 2. Default Player mode Selection trigger card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0B0E)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showPlayerDialog = true }
                .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = null,
                    tint = Color.Gray
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = LocaleHelper.translate("default_player", appLanguage),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = LocaleHelper.translate("player_mode", appLanguage) + activePlayerMode,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.PersonalVideo,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // 3. App Language selection trigger card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0B0E)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showLanguageDialog = true }
                .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = null,
                    tint = Color.Gray
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = LocaleHelper.translate("app_language_title", appLanguage),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = when (appLanguage) {
                                "ar" -> "العربية 🇸🇦"
                                "en" -> "English 🇺🇸"
                                else -> "Français 🇫🇷"
                            },
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // 4. Content Grid Display selection trigger card (2x2, 3x3, 4x4)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0B0E)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showGridDialog = true }
                .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = null,
                    tint = Color.Gray
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = LocaleHelper.translate("grid_columns_title", appLanguage),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = LocaleHelper.translate("current_grid", appLanguage) + "${gridColumns}x${gridColumns}",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.GridView,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // 5. Landscape/TV Mode toggle card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0B0E)),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = isLandscapeMode,
                    onCheckedChange = { viewModel.setLandscapeMode(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color(0xFF1B1E26)
                    )
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = LocaleHelper.translate("universal_landscape", appLanguage),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (isLandscapeMode) LocaleHelper.translate("enabled_tv", appLanguage) else LocaleHelper.translate("disabled_tv", appLanguage),
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Tv,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 6. Hidden Developer Dashboard trigger button
        Button(
            onClick = onNavigateToDeveloper,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B1E26)),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DeveloperMode,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = LocaleHelper.translate("developer_portal", appLanguage),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }

    // Appearance Modal Dialog
    if (showAppearanceDialog) {
        AlertDialog(
            onDismissRequest = { showAppearanceDialog = false },
            confirmButton = {
                Button(
                    onClick = { showAppearanceDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(LocaleHelper.translate("save", appLanguage), color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Text(
                    text = LocaleHelper.translate("app_theme", appLanguage),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                ) {
                    listOf(
                        Pair("Neon", if (appLanguage == "ar") "نيون سيبربنك (Cyan/Magenta)" else "Cyberpunk Neon"),
                        Pair("Cyan", if (appLanguage == "ar") "الأزرق السماوي (Cyan)" else "Sky Cyan"),
                        Pair("Magenta", if (appLanguage == "ar") "الوردي الفاقع (Magenta)" else "Hot Magenta"),
                        Pair("Amber", if (appLanguage == "ar") "الغروب الدافئ (Amber)" else "Warm Amber")
                    ).forEach { (code, title) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setPlayerTheme(code) }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = activeTheme == code,
                                onClick = { viewModel.setPlayerTheme(code) },
                                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                            )
                            Text(
                                text = title,
                                color = if (activeTheme == code) MaterialTheme.colorScheme.primary else Color.LightGray,
                                fontWeight = if (activeTheme == code) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            },
            containerColor = Color(0xFF0A0B0E),
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Default Player Type selection Modal Dialog
    if (showPlayerDialog) {
        AlertDialog(
            onDismissRequest = { showPlayerDialog = false },
            confirmButton = {
                Button(
                    onClick = { showPlayerDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(LocaleHelper.translate("save", appLanguage), color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Text(
                    text = LocaleHelper.translate("default_player", appLanguage),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                ) {
                    listOf(
                        Pair("Smart", if (appLanguage == "ar") "الوضع الذكي (تحديد تلقائي)" else "Smart Mode"),
                        Pair("m3u8", if (appLanguage == "ar") "رابط البث m3u8 (HLS Optimizer)" else "HLS Protocol (.m3u8)"),
                        Pair("ts", if (appLanguage == "ar") "رابط قنوات البث .TS (IPTV Mode)" else "IPTV Stream (.ts)"),
                        Pair("Standard", if (appLanguage == "ar") "التشغيل العام القياسي (Standard)" else "Standard Player")
                    ).forEach { (code, title) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setPlayerTypeMode(code) }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = activePlayerMode == code,
                                onClick = { viewModel.setPlayerTypeMode(code) },
                                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                            )
                            Text(
                                text = title,
                                color = if (activePlayerMode == code) MaterialTheme.colorScheme.primary else Color.LightGray,
                                fontWeight = if (activePlayerMode == code) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            },
            containerColor = Color(0xFF0A0B0E),
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Language selection Modal Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            confirmButton = {
                Button(
                    onClick = { showLanguageDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(LocaleHelper.translate("save", appLanguage), color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Text(
                    text = LocaleHelper.translate("select_language_dialog", appLanguage),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                ) {
                    listOf(
                        Pair("ar", "العربية 🇸🇦"),
                        Pair("en", "English 🇺🇸"),
                        Pair("fr", "Français 🇫🇷")
                    ).forEach { (code, title) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setAppLanguage(code) }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = appLanguage == code,
                                onClick = { viewModel.setAppLanguage(code) },
                                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                            )
                            Text(
                                text = title,
                                color = if (appLanguage == code) MaterialTheme.colorScheme.primary else Color.LightGray,
                                fontWeight = if (appLanguage == code) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            },
            containerColor = Color(0xFF0A0B0E),
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Content Grid columns selection Modal Dialog
    if (showGridDialog) {
        AlertDialog(
            onDismissRequest = { showGridDialog = false },
            confirmButton = {
                Button(
                    onClick = { showGridDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(LocaleHelper.translate("save", appLanguage), color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Text(
                    text = LocaleHelper.translate("select_grid_dialog", appLanguage),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                ) {
                    listOf(
                        Pair(2, if (appLanguage == "ar") "شبكة 2x2 للمرئيات الكبيرة" else if (appLanguage == "fr") "Grille 2x2" else "2x2 Large Grid"),
                        Pair(3, if (appLanguage == "ar") "شبكة 3x3 متكاملة وافتراضية" else if (appLanguage == "fr") "Grille 3x3 (Défaut)" else "3x3 Balanced Grid (Default)"),
                        Pair(4, if (appLanguage == "ar") "شبكة 4x4 عريضة ومطورة" else if (appLanguage == "fr") "Grille 4x4" else "4x4 Wide Grid")
                    ).forEach { (cols, title) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setGridColumns(cols) }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = gridColumns == cols,
                                onClick = { viewModel.setGridColumns(cols) },
                                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                            )
                            Text(
                                text = title,
                                color = if (gridColumns == cols) MaterialTheme.colorScheme.primary else Color.LightGray,
                                fontWeight = if (gridColumns == cols) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            },
            containerColor = Color(0xFF0A0B0E),
            shape = RoundedCornerShape(24.dp)
        )
    }
}

// ---------------------- SHARED SUB COMPOSABLES ----------------------

@Composable
fun CategoriesRow(
    categories: List<XtreamCategory>,
    selectedCategoryId: String?,
    onSelect: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF000000))
    ) {
        items(categories) { cat ->
            val isSelected = cat.categoryId == selectedCategoryId
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF14161E))
                    .border(
                        1.dp,
                        if (isSelected) Color.Transparent else Color.White.copy(alpha = 0.05f),
                        CircleShape
                    )
                    .clickable { onSelect(cat.categoryId) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = cat.categoryName,
                    color = if (isSelected) Color.Black else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun LiveItemCard(
    stream: LiveStreamItem,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0B0E)),
        modifier = modifier
            .padding(4.dp)
            .clickable { onClick() }
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(16.dp))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1B1E26)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!stream.streamIcon.isNullOrEmpty()) {
                        AsyncImage(
                            model = stream.streamIcon,
                            contentDescription = stream.name,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Tv,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stream.name,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.height(32.dp)
                )
            }

            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(2.dp)
                    .size(24.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = if (isFavorite) Color.Red else Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun MovieItemCard(
    movie: MovieStreamItem,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0B0E)),
        modifier = modifier
            .clickable { onClick() }
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Tall Vertical Aspect Ratio Poster Layout (NETFLIX STYLE)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color(0xFF1B1E26)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!movie.streamIcon.isNullOrEmpty()) {
                        AsyncImage(
                            model = movie.streamIcon,
                            contentDescription = movie.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MovieFilter,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    // Top Right Ultra HD Badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color.Black.copy(alpha = 0.75f))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "ULTRA HD",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = movie.name,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(8.dp)
                )
            }

            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
                    .size(24.dp)
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = if (isFavorite) Color.Red else Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun SeriesItemCard(
    series: SeriesItem,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0B0E)),
        modifier = modifier
            .clickable { onClick() }
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Tall Vertical Aspect Ratio Series Catalog Cover Layout (NETFLIX STYLE)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color(0xFF1B1E26)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!series.cover.isNullOrEmpty()) {
                        AsyncImage(
                            model = series.cover,
                            contentDescription = series.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.LocalMovies,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    // Top Right Season/Episodes Count Badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color.Black.copy(alpha = 0.75f))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "SERIES HD",
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = series.name,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(8.dp)
                )
            }

            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
                    .size(24.dp)
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = if (isFavorite) Color.Red else Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun SeriesDetailsOverlay(
    series: SeriesItem,
    episodes: List<com.example.data.SeriesEpisode>,
    onClose: () -> Unit,
    onEpisodeClick: (com.example.data.SeriesEpisode) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.94f))
            .clickable(enabled = false) {}
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.background(Color.White.copy(alpha = 0.05f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }

                Text(
                    text = series.name,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "قائمة الحلقات المتوفرة",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (episodes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("جاري سحب رمز وحلقات المسلسل من خادم المزود...", color = Color.Gray, fontSize = 13.sp)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(episodes) { episode ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0B0E)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onEpisodeClick(episode) }
                                .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                              ) {
                                Icon(
                                    imageVector = Icons.Default.PlayCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "الموسم الأول - الحلقة ${episode.episodeNum}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                    if (!episode.title.isNullOrEmpty()) {
                                        Text(
                                            text = episode.title,
                                            color = Color.Gray,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStatePlaceholder(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Inbox,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(54.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                color = Color.Gray,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
