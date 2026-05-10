package com.example.virelia.ui.screen

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import com.example.virelia.Database.DatabaseProvider
import com.example.virelia.Database.NoteEntity
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.Toast
import com.example.virelia.utils.isInternetAvailable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun HomeScreen(
    onAddClick: () -> Unit,
    onEditClick: (NoteEntity) -> Unit) {
    val context = LocalContext.current

    val db = DatabaseProvider.getDatabase(context)

    val notes by db.noteDao()
        .getAllNotes()
        .collectAsState(initial = emptyList())

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
                        note = note,

                        onEditClick = { note ->
                            onEditClick(note)
                        }
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
    note: NoteEntity,
    onEditClick: (NoteEntity) -> Unit
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
                        ),

                        fontSize = 12.sp
                    )
                }

                Text(
                    text = note.time,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // TITLE
            Text(
                text = note.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            // DESCRIPTION
            Text(
                text = note.desc,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(18.dp))

            // SHARE
            if (note.isShared) {
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
                val context = LocalContext.current

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {

                        if (isInternetAvailable(context)) {

                            val firestore = FirebaseFirestore.getInstance()

                            val db = DatabaseProvider.getDatabase(context)

                            val noteData = hashMapOf(

                                "title" to note.title,
                                "desc" to note.desc,
                                "time" to note.time
                            )

                            firestore.collection("stories")
                                .add(noteData)
                                .addOnSuccessListener {

                                    CoroutineScope(Dispatchers.IO).launch {

                                        db.noteDao().updateNote(
                                            note.copy(isShared = true)
                                        )
                                    }

                                    Toast.makeText(
                                        context,
                                        "Story berhasil di share",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                        } else {

                            Toast.makeText(
                                context,
                                "Tidak ada internet",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                ){

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
    if (showDeleteDialog) {

        AlertDialog(

            onDismissRequest = {
                showDeleteDialog = false
            },

            title = {
                Text("Delete Story")
            },

            text = {
                Text("Are you sure to delete this story?")
            },

            confirmButton = {

                TextButton(

                    onClick = {

                        showDeleteDialog = false

                        val db = DatabaseProvider.getDatabase(context)

                        CoroutineScope(Dispatchers.IO).launch {

                            db.noteDao().deleteNote(note)
                        }
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