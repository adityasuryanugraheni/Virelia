package com.example.virelia.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.virelia.Database.NoteEntity
import com.example.virelia.ui.viewmodel.ExploreViewModel
import java.net.URLEncoder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder

@Composable
fun ExploreScreen(
    navController: NavController,
    viewModel: ExploreViewModel = viewModel()
) {

    var searchText by remember { mutableStateOf("") }

    val publicNotes by viewModel.publicStories.collectAsState()
    val commentCounts by viewModel.commentCounts.collectAsState()

    // RELOAD SETIAP KALI KEMBALI KE EXPLORE
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadPublicStories()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(containerColor = Color(0xFFF5F5F7)) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Explore Story",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565FF)
            )

            Spacer(modifier = Modifier.height(10.dp))

            ExploreSearchBar(
                text = searchText,
                onTextChange = { searchText = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Public Trends",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(18.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(
                    publicNotes.filter {
                        it.title.contains(searchText, true)
                    }
                ) { note ->
                    ExploreNoteCard(
                        viewModel = viewModel,
                        note = note,
                        navController = navController,
                        username = note.username,
                        title = note.title,
                        desc = note.desc,
                        time = note.time,
                        commentCount = commentCounts[note.firestoreId] ?: 0,
                        onCommentClick = {
                            val encodedFirestoreId = URLEncoder.encode(
                                note.firestoreId, "UTF-8"
                            ).replace("+", "%20")
                            val encodedTitle = URLEncoder.encode(
                                note.title, "UTF-8"
                            ).replace("+", "%20")
                            val encodedDesc = URLEncoder.encode(
                                note.desc, "UTF-8"
                            ).replace("+", "%20")
                            val encodedUsername = URLEncoder.encode(
                                note.username, "UTF-8"
                            ).replace("+", "%20")

                            navController.navigate(
                                "detail/$encodedFirestoreId/$encodedTitle/$encodedDesc/$encodedUsername"
                            )
                        }
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
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Search public thoughts...", color = Color.Gray) },
        textStyle = LocalTextStyle.current.copy(color = Color.Black),
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
    viewModel: ExploreViewModel,
    note: NoteEntity,
    navController: NavController,
    username: String,
    title: String,
    desc: String,
    time: String,
    commentCount: Int,
    onCommentClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val encodedFirestoreId = URLEncoder.encode(
                    note.firestoreId, "UTF-8"
                ).replace("+", "%20")
                val encodedTitle = URLEncoder.encode(
                    title, "UTF-8"
                ).replace("+", "%20")
                val encodedDesc = URLEncoder.encode(
                    desc, "UTF-8"
                ).replace("+", "%20")
                val encodedUsername = URLEncoder.encode(
                    username, "UTF-8"
                ).replace("+", "%20")

                navController.navigate(
                    "detail/$encodedFirestoreId/$encodedTitle/$encodedDesc/$encodedUsername"
                )
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {

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

            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = android.text.Html
                    .fromHtml(
                        desc,
                        android.text.Html.FROM_HTML_MODE_COMPACT
                    )
                    .toString(),

                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {

                // ICON KOMENTAR
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onCommentClick() }
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = null,
                        tint =
                            if (note.commentCount > 0)
                                Color(0xFF1565FF)
                            else
                                Color.Gray
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$commentCount",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // TOMBOL LIKE
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { viewModel.toggleLike(note) }
                ) {
                    Icon(
                        imageVector =
                            if (note.isLiked)
                                Icons.Filled.Favorite
                            else
                                Icons.Filled.FavoriteBorder,

                        contentDescription = null,

                        tint =
                            if (note.isLiked)
                                Color.Red
                            else if (note.likeCount > 0)
                                Color.Red
                            else
                                Color.Gray
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${note.likeCount} likes",
                        color = if (note.isLiked) Color.Red else Color.Gray,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}