package com.example.virelia.ui.screen

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.virelia.Database.DatabaseProvider
import com.example.virelia.Database.NoteEntity
import com.example.virelia.utils.isInternetAvailable
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.virelia.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    onAddClick: () -> Unit,
    onEditClick: (NoteEntity) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {

    val context = LocalContext.current
    val notes by viewModel.notes.collectAsState()

    LaunchedEffect(Unit) {

        if (isInternetAvailable(context)) {

            viewModel.syncStories()
        }
    }

    Scaffold(

        containerColor = Color(0xFFF5F5F7),

        floatingActionButton = {

            FloatingActionButton(
                onClick = onAddClick,
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

            SearchBar()

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Your Collection",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(18.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                items(notes) { note ->

                    NoteCard(
                        note = note,

                        onEditClick = {
                            onEditClick(note)
                        },
                        viewModel = viewModel
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(100.dp))
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
            Text(
                text = "Search your thoughts...",
                color = Color.Gray
            )
        },

        leadingIcon = {

            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null
            )
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
fun NoteCard(
    note: NoteEntity,
    onEditClick: (NoteEntity) -> Unit,
    viewModel: HomeViewModel
) {

    val context = LocalContext.current

    var showMenu by remember {
        mutableStateOf(false)
    }

    var showDeleteDialog by remember {
        mutableStateOf(false)
    }

    Card(

        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {

                detectTapGestures(

                    onLongPress = {

                        showMenu = true
                    }
                )
            },

        shape = RoundedCornerShape(24.dp),

        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {

        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Surface(
                    shape = RoundedCornerShape(30.dp),

                    color =
                        if (note.isShared)
                            Color(0xFFE4EEFF)
                        else
                            Color(0xFFEFEFEF)
                ) {

                    Text(

                        text =
                            if (note.isShared)
                                "Public"
                            else
                                "Private",

                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 5.dp
                        )
                    )
                }

                Text(
                    text = note.time,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = note.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = note.desc,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(18.dp))

            // SHARE
            if (!note.isShared) {

                Row(

                    modifier = Modifier.clickable {

                        if (isInternetAvailable(context)) {

                            val noteData = hashMapOf(

                                "title" to note.title,
                                "desc" to note.desc,
                                "time" to note.time
                            )

                            viewModel.shareNote(

                                note = note,

                                onSuccess = {

                                    Toast.makeText(
                                        context,
                                        "Story berhasil di share",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },

                                onFailed = {

                                    Toast.makeText(
                                        context,
                                        "Gagal share",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )

                        } else {

                            Toast.makeText(
                                context,
                                "Tidak ada internet",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },

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
                        color = Color(0xFF1565FF)
                    )
                }

            } else {

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = Color.Red
                    )
                }
            }
        }

        DropdownMenu(

            expanded = showMenu,

            onDismissRequest = {
                showMenu = false
            }
        ) {

            // EDIT
            DropdownMenuItem(

                text = {
                    Text("Edit")
                },

                onClick = {

                    showMenu = false
                    onEditClick(note)
                }
            )

            // DELETE
            DropdownMenuItem(

                text = {
                    Text("Delete")
                },

                onClick = {

                    showMenu = false
                    showDeleteDialog = true
                }
            )
        }
    }

    // DIALOG DELETE
    if (showDeleteDialog) {

        AlertDialog(

            onDismissRequest = {
                showDeleteDialog = false
            },

            title = {
                Text("Delete Story")
            },

            text = {
                Text("Are you sure?")
            },

            confirmButton = {

                TextButton(

                    onClick = {

                        showDeleteDialog = false

                        viewModel.deleteNote(note)
                    }
                ) {

                    Text(
                        text = "Delete",
                        color = Color.Red
                    )
                }
            },

            dismissButton = {

                TextButton(

                    onClick = {

                        showDeleteDialog = false
                    }
                ) {

                    Text("Cancel")
                }
            }
        )
    }
}