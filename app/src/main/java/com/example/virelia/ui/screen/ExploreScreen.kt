package com.example.virelia.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email // Ikon untuk komentar di Explore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun ExploreScreen(navController: NavController) {

    // DATA DUMMY EXPLORE (Disesuaikan dengan pola Map di HomeScreen)
    val publicNotes = listOf(
        mapOf(
            "username" to "@alex_r",
            "title" to "Reflections on Minimalist Architecture",
            "desc" to "The intersection of silence and space creates a dialogue that most modern structures fail to acknowledge...",
            "time" to "2 hours ago",
            "comments" to "12"
        ),
        mapOf(
            "username" to "@jordan_design",
            "title" to "The Ethics of AI in Creative Tools",
            "desc" to "Are we losing the human touch or simply evolving the brush? As generative models become integrated...",
            "time" to "5 hours ago",
            "comments" to "28"
        ),
        mapOf(
            "username" to "@sam_ideas",
            "title" to "Morning Coffee Rituals",
            "desc" to "There is a specific rhythm to the world before 7 AM. The sound of water boiling, the ritualistic...",
            "time" to "Yesterday",
            "comments" to "56"
        )
    )

    Scaffold(
        containerColor = Color(0xFFF5F5F7), // Pola warna background HomeScreen

        floatingActionButton = {
            FloatingActionButton(
                onClick = {},
                containerColor = Color(0xFF1565FF) // Pola warna FAB HomeScreen
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {

            Spacer(modifier = Modifier.height(20.dp))

            // TITLE - Meniru gaya HomeScreen
            Text(
                text = "Explore Notes",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565FF)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // SEARCH BAR - Menggunakan pattern fungsi SearchBar kamu
            ExploreSearchBar()

            Spacer(modifier = Modifier.height(24.dp))

            // HEADER
            Text(
                text = "Public Trends",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(18.dp))

            // NOTE LIST - Menggunakan pola LazyColumn HomeScreen
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(publicNotes) { note ->
                    ExploreNoteCard(
                        navController = navController,
                        username = note["username"].toString(),
                        title = note["title"].toString(),
                        desc = note["desc"].toString(),
                        time = note["time"].toString(),
                        comments = note["comments"].toString()
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(90.dp))
                }
            }
        }
    }
}

@Composable
fun ExploreSearchBar() {
    var text by remember { mutableStateOf("") }

    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Search public thoughts...") },
        leadingIcon = {
            Icon(imageVector = Icons.Default.Search, contentDescription = null)
        },
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
        )
    )
}

@Composable
fun ExploreNoteCard(
    navController: NavController,
    username: String,
    title: String,
    desc: String,
    time: String,
    comments: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate("detail")
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {

            // TOP ROW (Username & Time)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = username,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565FF),
                    fontSize = 14.sp
                )
                Text(
                    text = time,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // TITLE
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            // DESCRIPTION
            Text(
                text = desc,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(18.dp))

            // FOOTER (Comments) - Mengikuti pola Row Share di HomeScreen
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = Color(0xFF1565FF),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$comments Comments",
                    color = Color(0xFF1565FF),
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }
    }
}