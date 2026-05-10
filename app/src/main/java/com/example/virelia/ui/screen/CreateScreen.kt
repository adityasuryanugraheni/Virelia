package com.example.virelia.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.InsertLink
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.platform.LocalContext
import com.example.virelia.Database.DatabaseProvider
import com.example.virelia.Database.NoteEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScreen(
    noteId: Int? = null,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val db = DatabaseProvider.getDatabase(context)

    var note by remember {
        mutableStateOf<NoteEntity?>(null)
    }

    LaunchedEffect(noteId) {

        if (noteId != null && noteId != -1) {

            CoroutineScope(Dispatchers.IO).launch {

                val data = db.noteDao().getNoteById(noteId)

                note = data
            }
        }
    }

    var title by remember {
        mutableStateOf("")
    }

    var content by remember {
        mutableStateOf("")
    }

    LaunchedEffect(note) {

        note?.let {

            title = it.title
            content = it.desc
        }
    }

    var selectedImageUri by remember {
        mutableStateOf<Uri?>(null)
    }

    // IMAGE PICKER
    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->

        selectedImageUri = uri
    }

    Scaffold(

        containerColor = Color(0xFFF5F5F7),

        topBar = {

            TopAppBar(

                title = {

                    Text(
                        text = "New Story",
                        fontWeight = FontWeight.SemiBold
                    )
                },

                navigationIcon = {

                    IconButton(
                        onClick = onBackClick
                    ) {

                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                },

                actions = {

                    TextButton(
                        onClick = {

                            val db = DatabaseProvider.getDatabase(context)

                            CoroutineScope(Dispatchers.IO).launch {

                                if (note == null) {

                                    val userId =
                                        FirebaseAuth.getInstance().currentUser?.uid ?: ""

                                    // ROOM LOCAL
                                    db.noteDao().insertNote(

                                        NoteEntity(
                                            title = title,
                                            desc = content,
                                            time = "Today"
                                        )
                                    )

                                    // FIRESTORE ONLINE
                                    val noteData = hashMapOf(

                                        "title" to title,
                                        "desc" to content,
                                        "time" to "Today",
                                        "userId" to userId
                                    )

                                    FirebaseFirestore.getInstance()
                                        .collection("stories")
                                        .add(noteData)
                                }else {

                                    // EDIT NOTE
                                    note?.copy(

                                        title = title,
                                        desc = content

                                    )?.let { updatedNote ->

                                        db.noteDao().updateNote(updatedNote)
                                    }
                                }

                                launch(Dispatchers.Main) {

                                    onBackClick()
                                }
                            }
                        },
                    ) {

                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            tint = Color.White
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = "Save",
                            color = Color(0xFF1565FF),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))
                }
            )
        },

        bottomBar = {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 18.dp, vertical = 10.dp),

                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // BOLD ICON
                IconButton(
                    onClick = {

                    }
                ) {

                    Icon(
                        imageVector = Icons.Default.FormatBold,
                        contentDescription = null
                    )
                }

                // IMAGE PICKER
                IconButton(
                    onClick = {

                        imageLauncher.launch("image/*")
                    }
                ) {

                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        tint = Color(0xFF1565FF)
                    )
                }

                // LINK
                IconButton(
                    onClick = {

                    }
                ) {

                    Icon(
                        imageVector = Icons.Default.InsertLink,
                        contentDescription = null
                    )
                }
            }
        }

    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {

            Spacer(modifier = Modifier.height(20.dp))

            // INFO TEXT
            Text(
                text = "Created March 24, 2025  •  Personal",
                color = Color.Gray,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // TITLE INPUT
            BasicTextField(
                value = title,

                onValueChange = {
                    title = it
                },

                modifier = Modifier.fillMaxWidth(),

                textStyle = TextStyle(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                ),

                decorationBox = { innerTextField ->

                    if (title.isEmpty()) {

                        Text(
                            text = "Title",
                            color = Color.LightGray,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    innerTextField()
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // CONTENT INPUT
            BasicTextField(
                value = content,

                onValueChange = {
                    content = it
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 300.dp),

                textStyle = TextStyle(
                    fontSize = 18.sp,
                    lineHeight = 28.sp,
                    color = Color.Black
                ),

                decorationBox = { innerTextField ->

                    if (content.isEmpty()) {

                        Text(
                            text = "Start writing...",
                            color = Color.LightGray,
                            fontSize = 18.sp
                        )
                    }

                    innerTextField()
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // DISPLAY IMAGE
            selectedImageUri?.let { uri ->

                Image(
                    painter = rememberAsyncImagePainter(uri),

                    contentDescription = null,

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(20.dp)),

                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(20.dp))
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

