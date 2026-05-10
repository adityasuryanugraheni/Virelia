package com.example.virelia.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.FavoriteBorder
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.platform.LocalFocusManager

@Composable
fun ExploreScreen(navController: NavController) {

    var searchText by remember {
        mutableStateOf("")
    }

    // DATA DUMMY EXPLORE
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
        containerColor = Color(0xFFF5F5F7)
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {

            Spacer(modifier = Modifier.height(20.dp))

            // TITLE
            Text(
                text = "Explore Story",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565FF)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // SEARCH BAR
            ExploreSearchBar(
                text = searchText,
                onTextChange = {
                    searchText = it
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // HEADER
            Text(
                text = "Public Trends",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(18.dp))

            // NOTE LIST
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                items(
                    publicNotes.filter {
                        it["title"].toString().contains(searchText, true)
                    }
                ) { note ->

                    ExploreNoteCard(
                        navController = navController,
                        username = note["username"].toString(),
                        title = note["title"].toString(),
                        desc = note["desc"].toString(),
                        time = note["time"].toString(),
                        comments = note["comments"].toString()
                    )
                }
            }
        }
    }
}

@Composable
fun ExploreSearchBar(
    text: String,
    onTextChange: (String) -> Unit
) {

    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = text,
        onValueChange = onTextChange,

        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),

        keyboardActions = KeyboardActions(
            onSearch = {
                focusManager.clearFocus()
            }
        ),

        modifier = Modifier.fillMaxWidth(),

        placeholder = {
            Text(
                "Search public thoughts...",
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
fun ExploreNoteCard(
    navController: NavController,
    username: String,
    title: String,
    desc: String,
    time: String,
    comments: String
) {

    var isLiked by rememberSaveable {
        mutableStateOf(false)
    }

    var likeCount by rememberSaveable {
        mutableStateOf(0)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate("detail")
            },

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
                fontWeight = FontWeight.Bold,
                color = Color.Black
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

            // FOOTER COMMENT & LIKE
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

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = comments,
                    color = Color.Gray,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.width(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        if (isLiked) {
                            isLiked = false
                            likeCount--
                        } else {
                            isLiked = true
                            likeCount++
                        }
                    }
                ) {

                    Icon(
                        imageVector =
                            if (isLiked)
                                Icons.Default.Favorite
                            else
                                Icons.Default.FavoriteBorder,

                        contentDescription = null,

                        tint =
                            if (isLiked)
                                Color.Red
                            else
                                Color.Gray
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Column {

                        Text(
                            text =
                                if (isLiked)
                                    "Liked"
                                else
                                    "Like",

                            color =
                                if (isLiked)
                                    Color.Red
                                else
                                    Color.Gray,

                            fontSize = 13.sp
                        )

                        Text(
                            text = "$likeCount likes",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}