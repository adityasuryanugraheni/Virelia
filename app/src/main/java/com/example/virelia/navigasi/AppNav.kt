package com.example.virelia.navigasi

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.*
import com.example.virelia.ui.screen.*

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

@Composable
fun AppNav() {

    val navController = rememberNavController()

    val items = listOf(
        BottomNavItem("home", "Home", Icons.Default.Home),
        BottomNavItem("explore", "Explore", Icons.Default.Search),
        BottomNavItem("profile", "Profile", Icons.Default.Person)
    )

    Scaffold(

        // BOTTOM NAVBAR
        bottomBar = {

            NavigationBar {

                val currentRoute =
                    navController.currentBackStackEntryAsState().value?.destination?.route

                items.forEach { item ->

                    NavigationBarItem(
                        selected = currentRoute == item.route,

                        onClick = {
                            navController.navigate(item.route) {

                                // Biar tidak numpuk halaman
                                popUpTo(navController.graph.startDestinationId)

                                launchSingleTop = true
                            }
                        },

                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title
                            )
                        },

                        label = {
                            Text(item.title)
                        },

                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Blue,
                            selectedTextColor = Color.Blue,
                            indicatorColor = Color(0xFFE3F2FD),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                    )
                    )
                }
            }
        }

    ) { paddingValues ->

        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {

            composable("home") {
                HomeScreen()
            }

            composable("explore") {
                ExploreScreen(navController)
            }

            composable("profile") {
                ProfileScreen()
            }

            composable("create") {
                CreateScreen()
            }

            composable("detail") {
                DetailScreen(navController)
            }

            composable("login") {
                LoginScreen(
                    onSignUpClick = {
                        navController.navigate("registrasi")
                    },
                    onLoginSuccess = {
                        navController.navigate("home")
                    }
                )
            }

            composable("registrasi") {
                RegistrasiScreen(
                    onLoginClick = {
                        navController.navigate("login")
                    }
                )
            }
        }
    }
}