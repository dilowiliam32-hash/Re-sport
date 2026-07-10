package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.SportsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContainer()
            }
        }
    }
}

@Composable
fun MainAppContainer() {
    val viewModel: SportsViewModel = viewModel()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val toastMessage by viewModel.activeReminderToast.collectAsState()

    // Control visibility of the bottom bar (hide inside player screen)
    val shouldShowBottomBar = currentRoute != "player"

    Scaffold(
        modifier = Modifier.fillMaxSize().background(SportSlateBg),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            if (shouldShowBottomBar) {
                NavigationBar(
                    containerColor = SportSurface,
                    contentColor = SportGreen,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("app_navigation_bar")
                ) {
                    NavigationBarItem(
                        selected = currentRoute == "home",
                        onClick = {
                            navController.navigate("home") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.SportsSoccer, contentDescription = "Schedule") },
                        label = { Text("Schedule", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = SportGreen,
                            indicatorColor = SportGreen,
                            unselectedIconColor = SportTextSecondary,
                            unselectedTextColor = SportTextSecondary
                        )
                    )

                    NavigationBarItem(
                        selected = currentRoute == "channels",
                        onClick = {
                            navController.navigate("channels") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Tv, contentDescription = "Live TV") },
                        label = { Text("Live TV", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = SportGreen,
                            indicatorColor = SportGreen,
                            unselectedIconColor = SportTextSecondary,
                            unselectedTextColor = SportTextSecondary
                        )
                    )

                    NavigationBarItem(
                        selected = currentRoute == "watchlist",
                        onClick = {
                            navController.navigate("watchlist") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Star, contentDescription = "Favorites") },
                        label = { Text("Favorites", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = SportGreen,
                            indicatorColor = SportGreen,
                            unselectedIconColor = SportTextSecondary,
                            unselectedTextColor = SportTextSecondary
                        )
                    )

                    NavigationBarItem(
                        selected = currentRoute == "news",
                        onClick = {
                            navController.navigate("news") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Article, contentDescription = "News") },
                        label = { Text("Highlights", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = SportGreen,
                            indicatorColor = SportGreen,
                            unselectedIconColor = SportTextSecondary,
                            unselectedTextColor = SportTextSecondary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(SportSlateBg)
        ) {
            NavHost(
                navController = navController,
                startDestination = "home"
            ) {
                composable("home") {
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigateToPlayer = { navController.navigate("player") }
                    )
                }
                composable("channels") {
                    ChannelsScreen(
                        viewModel = viewModel,
                        onNavigateToPlayer = { navController.navigate("player") }
                    )
                }
                composable("watchlist") {
                    WatchlistScreen(
                        viewModel = viewModel,
                        onNavigateToPlayer = { navController.navigate("player") }
                    )
                }
                composable("news") {
                    NewsScreen(viewModel = viewModel)
                }
                composable("player") {
                    PlayerScreen(
                        viewModel = viewModel,
                        onBackClick = {
                            // clear selections
                            viewModel.selectMatch(null)
                            viewModel.selectChannel(null)
                            navController.popBackStack()
                        }
                    )
                }
            }

            // Custom floating toast notification when saved/removed
            AnimatedVisibility(
                visible = toastMessage != null,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = if (shouldShowBottomBar) 16.dp else 32.dp)
                    .padding(horizontal = 24.dp)
            ) {
                toastMessage?.let { msg ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SportSurfaceVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (msg.contains("Reminder")) Icons.Default.Notifications else Icons.Default.Info,
                                contentDescription = "Info",
                                tint = SportGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = msg,
                                color = SportTextPrimary,
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(
                                onClick = { viewModel.dismissToast() },
                                colors = ButtonDefaults.textButtonColors(contentColor = SportGreen)
                            ) {
                                Text("OK", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Auto dismiss after 4 seconds
                    LaunchedEffect(msg) {
                        kotlinx.coroutines.delay(4000)
                        if (toastMessage == msg) {
                            viewModel.dismissToast()
                        }
                    }
                }
            }
        }
    }
}
