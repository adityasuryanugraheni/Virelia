package com.example.virelia.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.FavoriteBorder

@Composable
fun HomeScreen() {

    // DUMMY UI DATA
    val notes = listOf(
        mapOf(
            "category" to "Public",
            "title" to "The Future of Minimalist UI",
            "desc" to "In a world of constant digital noise...",
            "time" to "2 hours ago",
            "public" to true
        ),

        mapOf(
            "category" to "Private",
            "title" to "Grocery List for Dinner",
            "desc" to "Don't forget the organic basil...",
            "time" to "5 hours ago",
            "public" to false
        ),

        mapOf(
            "category" to "Private",
            "title" to "Daily Reflections",
            "desc" to "Reflection is the process...",
            "time" to "Yesterday",
            "public" to false
        )
    )

    Scaffold(

        containerColor = Color(0xFFF5F5F7),

        floatingActionButton = {

            FloatingActionButton(
                onClick = {},
                containerColor = Color(0xFF1565FF)
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

            // TITLE
            //Text(
                //text = "NoteShare",
                //fontSize = 28.sp,
                //fontWeight = FontWeight.Bold
            //)

            //Spacer(modifier = Modifier.height(20.dp))

            // SEARCH BAR
            SearchBar()

            Spacer(modifier = Modifier.height(24.dp))

            // HEADER
            Text(
                text = "Your Collection",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(18.dp))

            // NOTE LIST
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                items(notes) { note ->

                    NoteCard(
                        category = note["category"].toString(),
                        title = note["title"].toString(),
                        desc = note["desc"].toString(),
                        time = note["time"].toString(),
                        isPublic = note["public"] as Boolean
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
fun SearchBar() {

    var text by remember {
        mutableStateOf("")
    }

    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
        },

        modifier = Modifier.fillMaxWidth(),

        placeholder = {
            Text("Search your thoughts...",
                color = Color.Gray
                )
        },

        textStyle = LocalTextStyle.current.copy(
            color = Color.Black
        ),

        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Color.Gray
            )
        },

        shape = RoundedCornerShape(18.dp),

        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,

            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,

            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
        )
    )
}

@Composable
fun NoteCard(
    category: String,
    title: String,
    desc: String,
    time: String,
    isPublic: Boolean
) {

    Card(
        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(24.dp),

        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),

        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            // TOP ROW
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Surface(
                    shape = RoundedCornerShape(30.dp),

                    color =
                        if (isPublic)
                            Color(0xFFE4EEFF)
                        else
                            Color(0xFFEFEFEF)
                ) {

                    Text(
                        text = category,

                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 5.dp
                        ),

                        fontSize = 12.sp
                    )
                }

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

            // SHARE
            if (isPublic) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = null,
                        tint = Color.Gray
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = Color.Red
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = Color(0xFF1565FF)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "Share",
                        color = Color(0xFF1565FF),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}