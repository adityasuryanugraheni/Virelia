package com.example.virelia.navigasi

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.virelia.ui.screen.CreateScreen
import com.example.virelia.ui.screen.DetailScreen
import com.example.virelia.ui.screen.ExploreScreen
import com.example.virelia.ui.screen.HomeScreen
import com.example.virelia.ui.screen.LoginScreen
import com.example.virelia.ui.screen.ProfileScreen
import com.example.virelia.ui.screen.RegistrasiScreen
import com.google.firebase.auth.FirebaseAuth

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

@Composable
fun AppNav() {

    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()

    val startDestination =
        if (auth.currentUser != null) "home"
        else "login"

    val items = listOf(
        BottomNavItem("home", "Home", Icons.Default.Home),
        BottomNavItem("explore", "Explore", Icons.Default.Search),
        BottomNavItem("profile", "Profile", Icons.Default.Person)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (
                currentRoute != "login" &&
                currentRoute != "registrasi"
            ) {
                NavigationBar {
                    items.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
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
                            label = { Text(item.title) },
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
        }
    ) { paddingValues ->

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
        ) {

            // LOGIN
            composable("login") {
                LoginScreen(
                    onSignUpClick = { navController.navigate("registrasi") },
                    onLoginSuccess = {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }

            // REGISTRASI
            composable("registrasi") {
                RegistrasiScreen(
                    onLoginClick = { navController.navigate("login") },
                    onRegisterSuccess = {
                        navController.navigate("login") {
                            popUpTo("registrasi") { inclusive = true }
                        }
                    }
                )
            }

            // HOME
            composable("home") {
                HomeScreen(
                    onAddClick = { navController.navigate("create/-1") },
                    onEditClick = { note -> navController.navigate("create/${note.id}") }
                )
            }

            // EXPLORE
            composable("explore") {
                ExploreScreen(navController)
            }

            // PROFILE
            composable("profile") {
                ProfileScreen(
                    onLogout = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate("login") {
                            popUpTo(0)
                            launchSingleTop = true
                        }
                    }
                )
            }

            // CREATE
            composable("create/{noteId}") { backStackEntry ->
                val noteId = backStackEntry.arguments
                    ?.getString("noteId")
                    ?.toIntOrNull()
                CreateScreen(
                    noteId = noteId,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // DETAIL — TAMBAH firestoreId
            composable(
                route = "detail/{firestoreId}/{title}/{desc}/{username}/{imageUrl}"
            ) { backStackEntry ->
                DetailScreen(
                    navController = navController,
                    firestoreId = backStackEntry.arguments?.getString("firestoreId") ?: "",
                    title = backStackEntry.arguments?.getString("title") ?: "",
                    desc = backStackEntry.arguments?.getString("desc") ?: "",
                    username = backStackEntry.arguments?.getString("username") ?: "",
                    imageUrl = backStackEntry.arguments?.getString("imageUrl") ?: ""
                )
            }
        }
    }
}