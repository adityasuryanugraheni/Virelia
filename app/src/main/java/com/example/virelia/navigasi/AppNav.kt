package com.example.virelia.navigasi

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.virelia.ui.screen.CreateScreen
import com.example.virelia.ui.screen.HomeScreen
import com.example.virelia.ui.screen.LoginScreen
import com.example.virelia.ui.screen.ProfileScreen
import com.example.virelia.ui.screen.RegistrasiScreen
import com.example.virelia.ui.screen.DetailScreen
import com.example.virelia.ui.screen.ExploreScreen

@Composable
fun AppNav() {
    // 1. Inisialisasi Controller
    val navController = rememberNavController()

    // 2. Gunakan NavHost (Hapus logika 'when' manual agar tidak tumpang tindih)
    NavHost(
        navController = navController,
        startDestination = "detail"
    ) {

        // --- RUTE LOGIN ---
        composable("login") {
            LoginScreen(
                onSignUpClick = {
                    navController.navigate("registrasi")
                },
                onLoginSuccess = {
                    // Pindah ke home dan hapus history login agar tidak bisa balik lewat tombol back
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // --- RUTE REGISTRASI ---
        composable("registrasi") {
            RegistrasiScreen(
                onLoginClick = {
                    navController.navigate("login")
                }
            )
        }

        // --- RUTE HOME ---
        composable("home") {
            HomeScreen()
        }

        //Create
        composable("create") {
            CreateScreen()
        }

        //Explore
        composable("explore") {
            ExploreScreen(navController)
        }

        //Detail
        composable("detail") {
            DetailScreen(navController)
        }

        // Profile
        composable("profile") {

            ProfileScreen()
        }
    }
}